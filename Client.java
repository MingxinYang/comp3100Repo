import java.io.*;
import java.net.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.util.stream.Collectors;

class Job {
    String basicInfo = "";
    int subTime;
    int IDs;
    int RTime;
    int coreNum;
    int memSize;
    int disk;

    public static Job parseSimpleJob(String msg) {
        String[] info = msg.trim().split("\\s+");
        Job job = new Job();
        try {
            job.basicInfo = info[0];
            job.subTime = Integer.parseInt(info[1]);
            job.IDs = Integer.parseInt(info[2]);
            job.RTime = Integer.parseInt(info[3]);
            job.coreNum = Integer.parseInt(info[4]);
            job.memSize = Integer.parseInt(info[5]);
            job.disk = Integer.parseInt(info[6]);
        } catch (Exception e) {
            // ignore exception
        }
        return job;
    }

    public boolean isNone() {
        return basicInfo.equals(Command.NONE.getName());
    }

    public boolean isJCPL() {
        return basicInfo.equals(Command.JCPL.getName());
    }

    public String toString() {
        return "Job{" +
                "basicInfo='" + basicInfo + '\'' +
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
            throw new RuntimeException("Insufficient number of server properties");
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

    public boolean meetJobLevel(Job job) {
        if (memSize - job.memSize < 0) {
            return false;
        }
        if (coreNum - job.coreNum < 0) {
            return false;
        }
        return disk - job.disk >= 0;
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
