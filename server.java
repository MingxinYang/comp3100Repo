import java.io.*;
import java.net.*;

public class server {
    public static void main(String[] args) {
        try {
            ServerSocket ss = new ServerSocket(6666);

             Socket s = ss.accept();
            DataInputStream dis = new DataInputStream(s.getInputStream());//din

          
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());//dout

      
            String str = (String) dis.readUTF();
            System.out.println("Message= " +str);
            dos.writeUTF("GDAY");
            dos.flush();
            String str2=(String)dis.readUTF();
            System.out.println("Message = "+str2);
            dos.writeUTF("BYe");   
            dos.flush();
            dos.close();
            

            s.close();// Close the server socket
            ss.close();
            }
        catch (Exception e) {
            System.out.println(e);
        }
    }
} 
