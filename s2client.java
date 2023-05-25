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

    /**
     * command for the current task
     */
    String jobCmd = "";
    /**
     * Information about task submissions
     */
    int submitTime;
    int id;
    int estRunTime;
    int cores;
    int memory;
    int disk;

    /**
     * Initialize task-related properties according to index subscripts
     */
    public static Job parseSimpleJob(String msg) {
        String[] info = msg.trim().split("\\s+");
        Job job = new Job();
        try {
            job.jobCmd = info[0];
            job.submitTime = Integer.parseInt(info[1]);
            job.id = Integer.parseInt(info[2]);
            job.estRunTime = Integer.parseInt(info[3]);
            job.cores = Integer.parseInt(info[4]);
            job.memory = Integer.parseInt(info[5]);
            job.disk = Integer.parseInt(info[6]);
        } catch (Exception e) {
            // ignore exception
        }
        return job;
    }

