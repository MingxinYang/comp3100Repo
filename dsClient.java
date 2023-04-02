import java.io.*;
import java.net.*;


public class dsClient {
    public static void main(String[] args) {
         Socket s = new Socket("127.0.0.1", 50000);
        BufferedReader dis = new BufferedReader(new InputStreamReader(s.getInputStream()));
        PrintWriter dos = new PrintWriter(s.getOutputStream(), true);
        
        dos.println("HELO");
        dis.readLine();
        dos.println("AUTH " + System.getProperty("user.name"));
        dis.readLine();
        dos.println("REDY");

        String inputLine;
        while ((inputLine = dis.readLine()) != null) {
            System.out.println("Input line: " + inputLine);
            if (inputLine.startsWith("JOBN")) {
                String[] jobDetails = inputLine.split(" ");
                int jobId = Integer.parseInt(jobDetails[2]);
                int jobCores = Integer.parseInt(jobDetails[4]);
                int jobMemory = Integer.parseInt(jobDetails[5]);
                int jobDisk = Integer.parseInt(jobDetails[6]);

                   

                    for (int i = 0; i < numServers; i++) {
                    if(str!=null){
                        str = dis.readLine();
                        String[] serverInfo = str.split("\\s+");
                        int serverAvail = Integer.parseInt(serverInfo[4]);
                        if (serverAvail > maxAvail) {
                            serverType = serverInfo[0];
                            maxAvail = serverAvail;
                        }
                    }
                    }
                    dos.write(("OK\n").getBytes());
                    str = dis.readLine();

                    //schedule it when the msg ==JOBN
                if(lastMsg.contains("JOBN")){
                    dataOutput.write(("SCHD " +jobId+ " "+serverType+" "+n +"\n").getBytes());
                    str = dataInput.readLine();
                    System.out.println("Server: " +str);
                    n++;

                    if(n >= numServers){//when n higher than the NO. servers
                        n = 0;
                    }
                }
            }

            // ds-server sends QUIT and receive QUIT
            dataOutput.write(("QUIT\n").getBytes());
            str = dataInput.readLine();
            System.out.println("Server: " + str);

            // close the socket, I/O stream
            dataInput.close();
            dataOutput.close();
            s.close();

        } catch (Exception e) {
            System.out.println(e);
        }
   }
}
