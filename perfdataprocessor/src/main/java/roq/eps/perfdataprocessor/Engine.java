package roq.eps.perfdataprocessor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Engine {

    //list of objects
    static ArrayList<PerfModel> list = new ArrayList<PerfModel>();
    static JSONObject json;


    public static void main (String[] args)  {
        System.out.println("EPS performance data processor\n");
        if (args.length < 1) {
            System.out.println("Specify an input file prefix as an argument\n");
            System.exit(1);

        }

        String prefix = args[0];
        String data = prefix + "-performance_metrics.txt";
        String durations = prefix + "_run-result.json";
        String output = prefix + ".csv";
        
        System.out.println("Reading "+ data);

        //read loop
        try (InputStream is = new FileInputStream(data);
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));) {


            //regex patterns
            //i <3 regex
            Pattern scenarioPat = Pattern.compile("scenario=(.*?)\\,");
            Pattern typePat = Pattern.compile("^([a-z]+)\\,");
            Pattern memPat = Pattern.compile("used_percent=(.*?)[\\, ]");
            Pattern cpuPat = Pattern.compile("usage_idle=(.*?)[\\, ]");
            

            
            String line = null; 
            while (( line = br.readLine()) != null){
 

                Matcher scenarioMatch = scenarioPat.matcher(line);
                if(scenarioMatch.find()){

                    //get the perfModel object from the list - create a new one if necessary
                    PerfModel current = getModel(scenarioMatch.group(1));


                    //for this line, find out what metric is captured and process accordingly
                    Matcher typeMatch = typePat.matcher(line);

                    if(typeMatch.find()){
                            String type = typeMatch.group(1);

                            switch(type) {
                                case "mem":
                                    Matcher memMatch = memPat.matcher(line);
                                    if (memMatch.find()){
                                        Double mem = Double.parseDouble(memMatch.group(1));
                                        current.updateMem(mem);
                                    }
                                   break;

                                   case "cpu":
                                   Matcher cpuMatch = cpuPat.matcher(line);
                                   if (cpuMatch.find()){
                                       //100 - idle = usage
                                       Double cpu = 100D - Double.parseDouble(cpuMatch.group(1));
                                       current.updateCpu(cpu);
                                   }
                                  break;
                                //add more handlers for data here

                                default:
                                   //do nothing
                             }
                    }
                }
            }
        }
        catch (IOException e) {
            System.out.println("Exception:" + e);
        }

        System.out.println("Reading "+ durations);
        //read the durations into memory
        try {
            Object object = new JSONTokener(new FileReader(durations)).nextValue(); 
            json = (JSONObject)object;
        } catch (FileNotFoundException e) {
            System.out.println("Exception" + e);
        }
        

        


        //dump everything to a file
        
        System.out.println("Writing "+ output);

        File file = new File(output);

        try (FileOutputStream fos = new FileOutputStream(file);
        OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF8");
        BufferedWriter writer = new BufferedWriter(osw)) {


            writer.write("testname, duration, cpuHigh, cpuLow, cpuAvg, memHigh, memLow, memAvg\n");
            Iterator i = list.iterator();
            while (i.hasNext()) {
                PerfModel current = (PerfModel)i.next();
                writer.write(current.scenario + ", " + getDuration(current.scenario)+ ", " +current.cpuMax + ", " + current.cpuMin+ ", " + current.cpuAvg + ", " + current.memMax + ", " + current.memMin+ ", " + current.memAvg + "\n");
            }

            writer.flush();
            writer.close();

        }
        catch (IOException e) {
            System.out.println("Exception:" + e);
        }

        System.out.println("Done!");
    
    }

    static PerfModel getModel(String name) {

        Iterator i = list.iterator();
        while (i.hasNext()) {
           PerfModel current = (PerfModel)i.next();
           if (current.scenario.contentEquals(name)) {
               return current;
           }
        }

        //not found - create it
        PerfModel newModel = new PerfModel(name);
        list.add(newModel);

        return newModel;


    }

    static String getDuration(String name) {

        name=name.replace("_", " ");

        JSONArray features = (JSONArray)json.get("features");

        for(Object feature : features)
        {
            JSONObject featureJson = (JSONObject) feature;
            JSONArray scenarios = (JSONArray)featureJson.get("scenarios");
            for(Object scenario : scenarios)
            {
                JSONObject scenarioJson = (JSONObject) scenario;

                if (scenarioJson.get("name").toString().contentEquals(name)) {
                    return scenarioJson.get("duration").toString();
                }
            }
        }

        return "not found!";
    }

}
