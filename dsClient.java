import java.io.*;
import java.net.*;

public class dsClient {
    public static void main(String[] args) {
        try {
            //initialize job related data variables below
            int maxAvail = 0;
            int numServers = 0;
            String serverType = "";
            int jobID = 0;
            int n = 0;

            //create a socket at localhost of port 50000
            Socket s = new Socket("127.0.0.1", 50000);

         DataOutputStream dataOutput = new DataOutputStream(s.getOutputStream());
         BufferedReader dataInput =new BufferedReader(new InputStreamReader(s.getInputStream()));

            dataOutput.write(("HELO" + "\n").getBytes());
            String str = dataInput.readLine();
            System.out.println("message= " + str);

            //authenticate using my Ubuntu username
            String username="mingxinyang";
            dataOutput.write(("AUTH " + username+"\n").getBytes());
            str = dataInput.readLine();
            System.out.println("Server: " + str);

            //execute below codes when system does not send NONE
            while(!str.contains("NONE")){
                // send REDY to ds-server
                dataOutput.write(("REDY\n").getBytes());
                str = dataInput.readLine();
                System.out.println("Server: " + str);
                String lastMsg = str;

                // if the message is JOBN, get the job ID
                if(str.contains("JOBN")){
                    String[] arr = str.split(" ");
                    jobID = Integer.parseInt(arr[2]);
                }

                //obtain the maxAvail core and the NO. of it
                if(maxAvail == 0){
                    dataOutput.write(("GETS All\n").getBytes());
                    str = dataInput.readLine();
                    System.out.println("Server: " + str);
                    String[] data = str.split(" ");
                    int numRecords = Integer.parseInt(data[1]);
                    dataOutput.write(("OK\n").getBytes());

                    for(int i=0; i<numRecords; i++){
                        str = dataInput.readLine();
                        System.out.println("Server: " + str);

                        String[] arr = str.split(" ");
                        int coreCount = Integer.parseInt(arr[4]);

                        if(coreCount > maxAvail){
                           maxAvail = coreCount;
                            serverType = arr[0];
                            numServers = 0;
                        } else if(coreCount == maxAvail && serverType.equals(arr[0])){
                            numServers++;
                        }
                    }

                    dataOutput.write(("OK\n").getBytes());
                    str = (String)dataInput.readLine();
                    System.out.println("Server: " + str);
                }

                //schedule it when the msg ==JOBN
                if(lastMsg.contains("JOBN")){
                    dataOutput.write(("SCHD " +jobID+ " "+serverType+" "+n +"\n").getBytes());
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
