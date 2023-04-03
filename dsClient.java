import java.io.*;
import java.net.*;

public class dsClient {
    public static void main(String[] args) {
        try {
            int maxCores = 0;
            int totalServers = 0;
            String sType = "";
            int jobId = 0;
            int currentIndex = 0;

            Socket socket = new Socket("localhost", 50000);

            DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
            BufferedReader dataIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            dataOut.write(("HELO" + "\n").getBytes());
            String response = dataIn.readLine();
            System.out.println("Received: " + response);

            String user = "mingxinyang";
            dataOut.write(("AUTH " + user + "\n").getBytes());
            response = dataIn.readLine();
            System.out.println("Server: " + response);

            while (!response.contains("NONE")) {
                dataOut.write(("REDY\n").getBytes());
                response = dataIn.readLine();
                System.out.println("Server: " + response);
                String previousMsg = response;

                if (response.contains("JOBN")) {
                    String[] arr = response.split(" ");
                    jobId = Integer.parseInt(arr[2]);
                }

                if (maxCores == 0) {
                    dataOut.write(("GETS All\n").getBytes());
                    response = dataIn.readLine();
                    System.out.println("Server: " + response);
                    String[] data = response.split(" ");
                    int numRecords = Integer.parseInt(data[1]);
                    dataOut.write(("OK\n").getBytes());

                    for (int i = 0; i < numRecords; i++) {
                        response = dataIn.readLine();
                        System.out.println("Server: " + response);

                        String[] arr = response.split(" ");
                        int coreCount = Integer.parseInt(arr[4]);

                        if (coreCount > maxCores) {
                            maxCores = coreCount;
                            sType = arr[0];
                            totalServers = 0;
                        } else if (coreCount == maxCores && sType.equals(arr[0])) {
                            totalServers++;
                        }
                    }

                    dataOut.write(("OK\n").getBytes());
                    response = dataIn.readLine();
                    System.out.println("Server: " + response);
                }

                if (previousMsg.contains("JOBN")) {
                    dataOut.write(("SCHD " + jobId + " " + sType + " " + currentIndex + "\n").getBytes());
                    response = dataIn.readLine();
                    System.out.println("Server: " + response);
                    currentIndex++;

                    if (currentIndex >= totalServers) {
                        currentIndex = 0;
                    }
                }
            }

            dataOut.write(("QUIT\n").getBytes());
            response = dataIn.readLine();
            System.out.println("Server: " + response);

            dataIn.close();
            dataOut.close();
            socket.close();

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
