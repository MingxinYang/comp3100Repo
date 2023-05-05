import java.io.*;
import java.net.*;

public class dsClient {
    public static void main(String[] args) {
        try {
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

                int taskID = 0;
                if (response.contains("JOBN")) {
                    String[] arr = response.split(" ");
                    taskID = Integer.parseInt(arr[2]);
                }

                dataOut.write(("GETS Capable\n").getBytes());
                response = dataIn.readLine();
                System.out.println("Server: " + response);

                String[] data = response.split(" ");
                int numRecords = Integer.parseInt(data[1]);

                if (numRecords > 0) {
                    dataOut.write(("OK\n").getBytes());

                    for (int i = 0; i < numRecords; i++) {
                        response = dataIn.readLine();
                        System.out.println("Server: " + response);

                        String[] arr = response.split(" ");
                        String sType = arr[0];

                        dataOut.write(("OK\n").getBytes());
                        response = dataIn.readLine();
                        System.out.println("Server: " + response);

                        if (response.contains("OK")) {
                            dataOut.write(("SCHD " + taskID + " " + sType + " " + currentIndex + "\n").getBytes());
                            response = dataIn.readLine();
                            System.out.println("Server: " + response);
                            currentIndex++;
                            break;
                        }
                    }
                } else {
                    dataOut.write(("OK\n").getBytes());
                    response = dataIn.readLine();
                    System.out.println("Server: " + response);
                }

                if (currentIndex >= numRecords) {
                    currentIndex = 0;
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

