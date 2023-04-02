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

                    // Schedule the job
                    String schdMsg = "SCHD " + jobDetails[2] + " " + serverType + " " + jobDetails[4] + " " + jobDetails[5] + "\n";
                    dos.write((schdMsg).getBytes());
                    str = dis.readLine();
                }
            }

            dos.write(("QUIT"+"\n").getBytes());
            str = dis.readLine();

            dis.close();
            dos.close();
            s.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
}
