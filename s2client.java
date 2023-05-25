import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilderFactory;

 class Job {

 
    String basicInfo = "";
//task sub info
    int subTime;
    int IDs;
    int RTime;
    int coreNum;
    int memSize
    int disk;

   // Initialize the properties of job-data
    public static Job parseSimpleJob(String msg) {
        String[] info = msg.trim().split("\\s+");
        Job job = new Job();
        try {
            job.basicInfo = info[0];
            job.subTime = Integer.parseInt(info[1]);
            job.IDs = Integer.parseInt(info[2]);
            job.RTime= Integer.parseInt(info[3]);
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
