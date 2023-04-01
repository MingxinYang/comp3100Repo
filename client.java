import java.io.*;
import java.net.*;

public class client {
    public static void main(String[] args) {
        try {
            Socket s = new Socket("localhost", 6666);

           
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());//dout
            DataInputStream dis = new DataInputStream(s.getInputStream());//din
            dos.writeUTF("Hi SERVERRR");


            String str = (String) dis.readUTF();//str

            System.out.println("Sent= " + str);
            dos.writeUTF("Bye");
            String str2 =(String)dis.readUTF();
            System.out.println("Message= " + str2);
            
            dos.close();
            
                // Close the connection with the server
                s.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
