import java.io.*;
import java.net.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.util.stream.Collectors;

class Job {
    String basicInfor = "";
    int subTime;
    int IDs;
    int RTime;
    int coreNum;
    int memSize;
    int disk;

    public static Job parseSimpleJb(String msg) {
        String[] info = msg.trim().split("\\s+");
        Job job = new Job();
        try {
            job.basicInfor = info[0];
            job.subTime = Integer.parseInt(info[1]);
            job.IDs = Integer.parseInt(info[2]);
            job.RTime = Integer.parseInt(info[3]);
            job.coreNum = Integer.parseInt(info[4]);
            job.memSize = Integer.parseInt(info[5]);
            job.disk = Integer.parseInt(info[6]);
        } catch (Exception e) {
            // ignroe exception
        }
        return job;
    }

    public boolean isNone() {
        return basicInfor.equals(Command.NONE.getName());
    }

    public boolean isJCPL() {
        return basicInfor.equals(Command.JCPL.getName());
    }

    public String toString() {
        return "Jb{" +
                "basicInfo='" + basicInfor + '\'' +
                ", subTime=" + subTime +
                ", IDs=" + IDs +
                ", RTime=" + RTime +
                ", coreNum=" + coreNum +
                ", memSize=" + memSize +
                ", disk=" + disk +
                '}';
    }
}

enum JobState {
    Submitted(0), Waiting(1), Running(2), Suspended(3), Completed(4), PreEmpted(5), Failed(6), Killed(7);

    JobState(int i) {
    }

    public static JobState parse(int type) {
        if (type < 0 || type >= values().length) {
            return null;
        }
        return values()[type];
    }
}

class Server {
    String type;
    int IDs;
    ServerState state;
    int curStartTime;
    int coreNum;
    int memSize;
    int disk;
    int wJobs;
    int rJobs;
    int bootTime;
    int estTime;
    float hourlyRate;
    static Map<String, Integer> bootTimeMap = null;
    static Map<String, Float> hourlyRateMap = null;
    static final int SIMPLE_SERVER_FIELDS = 9;

    private Server() {
        if (bootTimeMap == null) {
            buildBootTime();
        }
    }

    public static Server parseSimpleServer(String msg) {
        String[] info = msg.trim().split("\\s+");
        Server server = new Server();
        if (info.length < SIMPLE_SERVER_FIELDS) {
            throw new RuntimeException("NOt Enought server properties");
        }
        try {
            server.type = info[0];
            server.IDs = Integer.parseInt(info[1]);
            server.state = ServerState.parse(info[2]);
            server.curStartTime = Integer.parseInt(info[3]);
            server.coreNum = Integer.parseInt(info[4]);
            server.memSize = Integer.parseInt(info[5]);
            server.disk = Integer.parseInt(info[6]);
            server.wJobs = Integer.parseInt(info[7]);
            server.rJobs = Integer.parseInt(info[8]);
            server.bootTime = bootTimeMap.get(server.type);
            server.hourlyRate = hourlyRateMap.get(server.type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return server;
    }

    public boolean meetJbLevel(Job jb) {
        if (memSize - jb.memSize < 0) {
            return false;
        }
        if (coreNum - jb.coreNum < 0) {
            return false;
        }
        return disk - jb.disk >= 0;
    }

    public String toString() {
        return String.format("[%s, %d, %s, %d, %d, %d, %d, %d, %d]",
                type, IDs, state, curStartTime, coreNum, memSize, disk, wJobs, rJobs);
    }

    public static void buildBootTime() {
        bootTimeMap = new HashMap<>();
        hourlyRateMap = new HashMap<>();
        NodeList list = null;
        try {
            list = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse("ds-system.xml").getElementsByTagName("servers");
            for (int i = 0; i < list.getLength(); i++) {
                Element element = (Element) list.item(i);
                NodeList childNodes = element.getChildNodes();
                for (int j = 0; j < childNodes.getLength(); j++) {
                    NamedNodeMap attributes = childNodes.item(j).getAttributes();
                    if (attributes == null) {
                        continue;
                    }
                    String type = attributes.getNamedItem("type").getNodeValue();
                    int bootTime = Integer.parseInt(attributes.getNamedItem("bootupTime").getNodeValue());
                    float hourlyRate = Float.parseFloat(attributes.getNamedItem("hourlyRate").getNodeValue());
                    bootTimeMap.put(type, bootTime);
                    hourlyRateMap.put(type, hourlyRate);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }
}

enum ServerState {
    Inactive, Booting, Idle, Active, Unavailable;

    static ServerState parse(String state) {
        switch (state) {
            case "inactive":
                return Inactive;
            case "booting":
                return Booting;
            case "idle":
                return Idle;
            case "active":
                return Active;
            case "unavailable":
                return Unavailable;
            default:
                throw new RuntimeException("Unsupported server state");
        }
    }
}

interface Scheduler {
    void schedule() throws IOException;
    void scheduleOneJb(Job jb, Server server) throws IOException;
}

public class Client implements Scheduler {
    private final Socket socket;
    private final BufferedReader fromServer;
    private final BufferedWriter toServer;
    private final PrintStream out;
    private final String USER_NAME = System.getProperty("user.name");
    private final String end;

    public Client(String host, int port, PrintStream out, String end) {
        try {
            socket = new Socket(InetAddress.getByName(host), port);
            fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            toServer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.out = out;
            this.end = end.isEmpty() ? "\n" : end;
        } catch (IOException e) {
            throw new RuntimeException("Connect to Server fail: " + host + ":" + port);
        }
    }

    @Override
    public void schedule() throws IOException {
        init();
        out.println("Connection Success!");
        Job jb = getOneJb();
        while (!jb.isNone()) {
            jb = getOneJb();
            if (jb.isNone()) {
                break;
            }
            if (jb.isJCPL()) {
                continue;
            }
            List<Server> jbServers = getServersByCap(jb);
            if (jbServers.isEmpty()) {
                break;
            }
            Server server = null;
            if (allInactive(jbServers) && !jbServers.isEmpty()) {
                server = jbServers.stream().min(Comparator.comparingInt(s -> s.bootTime)).get();
            } else {
                Optional<Server> first = jbServers.stream().filter(s -> s.state == ServerState.Idle).findFirst();
                if (first.isPresent()) {
                    server = first.get();
                } else {
                    jbServers = getEstTime(jbServers, 5);
                    server = getBestServer(jbServers, jb);
                }
            }
            scheduleOneJb(jb, server);
        }
        sendMsg(Command.QUIT.getName());
        receiveMsg(false);
    }

    private boolean allInactive(List<Server> servers) {
        return servers.stream().allMatch(s -> s.state == ServerState.Inactive);
    }

    private List<Server> getEstTime(List<Server> servers, int limit) throws IOException {
        for (Server server : servers) {
            sendMsg(Command.EJWT.getName(), server.type, server.IDs);
            server.estTime = Integer.parseInt(receiveMsg(false));
        }
        servers.sort(Comparator.comparingInt(s -> s.estTime));
        return new ArrayList<>(servers.subList(0, Math.min(limit, servers.size())));
    }

    private Server getBestServer(List<Server> servers, Job jb) {
        List<Server> bestServers = servers.stream()
            .sorted(Comparator.comparingDouble((Server s) -> s.hourlyRate))
            .limit(3)
            .collect(Collectors.toList());

        return bestServers.stream()
                .min(Comparator.comparingDouble(s -> s.hourlyRate))
                .orElseThrow(NoSuchElementException::new);
    }

    private List<Server> getServersByCap(Job jb) throws IOException {
        sendMsg(Command.GETS.getName(), "Capable", jb.coreNum, jb.memSize, jb.disk);
        String serverNum = receiveMsg(false);
        sendMsg(Command.OK.getName());
        String[] data = serverNum.split("\\s+");
        int numOfServer = Integer.parseInt(data[1]);
        if (numOfServer == -1) {
            sendMsg(Command.QUIT.getName());
            receiveMsg(false);
            return new ArrayList<>();
        }
        List<Server> servers = parseNServer(numOfServer);
        sendMsg(Command.OK.getName());
        receiveMsg(false);
        return servers;
    }

    private List<Server> parseNServer(int numOfServer) throws IOException {
        List<Server> availableServers = new ArrayList<>();
        for (int i = 0; i < numOfServer; i++) {
            String serverInfo = receiveMsg(false);
            Server server = Server.parseSimpleServer(serverInfo);
            availableServers.add(server);
        }
        return availableServers;
    }

    private Job getOneJb() throws IOException {
        sendMsg(Command.REDY.getName());
        String jobInfo = receiveMsg(false);
        return Job.parseSimpleJb(jobInfo);
    }

    @Override
    public void scheduleOneJb(Job jb, Server server) throws IOException {
        sendMsg(Command.SCHD.getName(), jb.IDs, server.type, server.IDs);
        receiveMsg(false);
    }

    public void init() throws IOException {
        sendMsg(Command.HELO.getName());
        String msg = receiveMsg(true);
        if (!msg.equals(Command.OK.getName())) {
            throw new RuntimeException("Init Server fail");
        }
        sendMsg(Command.AUTH.getName(), USER_NAME);
        msg = receiveMsg(false);
        if (!msg.equals(Command.OK.getName())) {
            throw new RuntimeException("Auth Server fail");
        }
    }

    public void sendMsg(String msg, Object... args) throws IOException {
        StringBuilder cmd = new StringBuilder(msg);
        for (Object arg : args) {
            cmd.append(" ").append(arg);
        }
        toServer.write(cmd + end);
        toServer.flush();
    }

    public String receiveMsg(boolean isPrint) throws IOException {
        StringBuilder resp = new StringBuilder();
        while (true) {
            char[] buf = new char[1];
            int n = fromServer.read(buf);
            if (n != 1) {
                throw new RuntimeException("Read from server fail");
            }
            if (end.equals(buf[0] + "")) {
                break;
            }
            resp.append(buf);
        }
        if (isPrint) {
            out.println("SERVER: " + resp);
        }
        return resp.toString();
    }

    public void shutdown() throws IOException {
        out.println("terminating");
        socket.close();
    }

    public static void main(String[] args) throws IOException {
        String host = "127.0.0.1";
        int port = 50000;
        String end = "";
        try {
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "-h":
                        host = InetAddress.getByName(args[i + 1]).getHostAddress();
                        break;
                    case "-p":
                        port = Integer.parseInt(args[i + 1]);
                        break;
                    case "-n":
                        end = "\n";
                        break;
                }
            }
        } catch (Exception e) {
            System.err.println("Command line parameter input error");
            System.out.println("java MyClient -h <host> -p <port> -n");
            return;
        }
        Client myClient = new Client(host, port, System.out, end);
        myClient.schedule();
        myClient.shutdown();
    }
}

enum Command {
    HELO("HELO"), AUTH("AUTH"), QUIT("QUIT"),
    REDY("REDY"), DATA("DATA"),
    JOBN("JOBN"), JOBP("JOBP"), JCPL("JCPL"), RESF("RESF"), RESR("RESR"), NONE("NONE"),
    GETS("GETS"), SCHD("SCHD"), CNTJ("CNTJ("), EJWT("EJWT"), LSTJ("LSTJ"), PSHJ("PSHJ"),
    MIGJ("MIGJ"), KILJ("KILJ"), TERM("TERM"),
    ERR("ERR"),
    OK("OK");
    private final String name;

    Command(String schd) {
        name = schd;
    }

    public String getName() {
        return name;
    }
}

