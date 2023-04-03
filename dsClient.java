import java.io.*;
import java.net.*;

public class dsClient {
    public static void main(String[] args) {
        try {//variables to hold job-related data
            int maxCPU = 0;
            int totalServers;   
            //establish socket connection and IO stream
            Socket socket = new Socket("localhost", 50000);
            DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
    BufferedReader dataIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             //basic communication according to the pseudo-code provided
            String sType=" ";
            dataOut.write(("HELO" + "\n").getBytes());
            String response = dataIn.readLine();
            System.out.println("Received: " + response);
             //AUTH process according to the pseudo code
            int taskID;
            String user = "mingxinyang";
            dataOut.write(("AUTH " + user + "\n").getBytes());
            response = dataIn.readLine();
            System.out.println("Server: " + response);
            //when the server does not send NONE, meaning there are still tasks left
            totalServers =0;
            int currentIndex = 0;
            while (!response.contains("NONE")) {     
                dataOut.write(("REDY\n").getBytes());
                response = dataIn.readLine();
                System.out.println("Server: " + response);
                String previousMsg = response;
             //server says "JOBN", then client gets the taskID value
             taskID=0;
                if (response.contains("JOBN")) {
                    String[] arr = response.split(" ");
                    taskID = Integer.parseInt(arr[2]);
                }
             //asking server to show info about all available servers
                if (maxCPU == 0) {
                    dataOut.write(("GETS All\n").getBytes());
                    response = dataIn.readLine();
                    System.out.println("Server: " + response);
                    String[] data = response.split(" ");
                    int numRecords = Integer.parseInt(data[1]);
                    dataOut.write(("OK\n").getBytes());
                 //using a loop to select the server with the most cores, LRR
                 //a process of selecting the server to handle the task
                    for (int i = 0; i < numRecords; i++) {
                        response = dataIn.readLine();
                        System.out.println("Server: " + response);

                        String[] arr = response.split(" ");
                        int coreCount = Integer.parseInt(arr[4]);
                 //compare the maxCPU with the core of each single server
                        if (coreCount >maxCPU) {
                           maxCPU = coreCount;
                            sType = arr[0];
                            totalServers = 0;
                        } else if (coreCount == maxCPU && sType.equals(arr[0])) {
                            totalServers++;
                        }
                    }
                    //sends OK,acknowledging that the compare process is over
                    dataOut.write(("OK\n").getBytes());
                    response = dataIn.readLine();
                    System.out.println("Server: " + response);
                }
                 //if the message includes JOBN, 
                 //client schedules the task on selected server
                if (previousMsg.contains("JOBN")) {
                dataOut.write(("SCHD " + taskID+ " "+sType+" " +currentIndex+ "\n").getBytes());
                    response = dataIn.readLine();
                    System.out.println("Server: " + response);
                    currentIndex++;
                //Index==0, when it exceeds the no. of servers with max core counts
                    if (currentIndex >= totalServers) {
                        currentIndex = 0;
                    }
                }
            }
                 //if the NONE message is sent by the server, quit the loop and connection
            dataOut.write(("QUIT\n").getBytes());
            response = dataIn.readLine();
            System.out.println("Server: " + response);
                 //close all connections
            dataIn.close();
            dataOut.close();
            socket.close();

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
