import java.io.*;
import java.net.*;


public class dsClient {
    public static void main(String[] args) {
        try {
            Socket s = new Socket("127.0.0.1", 50000);

            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
            BufferedReader dis = new BufferedReader(new InputStreamReader(s.getInputStream()));

            dos.write(("HELO" + "\n").getBytes());
            String str = (String)dis.readLine();
            System.out.println("message= " + str);//added
            
            dos.write(("AUTH " + "mingxinyang"+ "\n").getBytes());
            str = dis.readLine();


             int numServers = Integer.parseInt(serverDetails[1]);
                    String type = "";
                    int maxAvail = 0;
             int n=0; //n=j
            while(!str.contains("NONE")){
                dos.write(("REDY"+"\n").getBytes());
                str = dis.readLine();

               

                if (str.startsWith("JOBN")) {
                    String[] jobDetails = str.split("\\s+");

                    // Get the largest server type
                    dos.write(("GETS All " + jobDetails[4] + " " + jobDetails[5] + " " + jobDetails[6] + "\n").getBytes());
                    str = dis.readLine();
                    String[] serverDetails = str.split("\\s+");
                   

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
