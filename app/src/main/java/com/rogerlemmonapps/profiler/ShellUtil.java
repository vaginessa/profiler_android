package com.rogerlemmonapps.profiler;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by r on 9/28/2014.
 */
public class ShellUtil {

    public static String[] RunAsRoot(String[] cmds){
        List<String> output = new ArrayList<String>();
        String error = "";
        try{
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            InputStream stderr = p.getErrorStream();
            InputStream stdout = p.getInputStream();
            for (String tmpCmd : cmds) {
                os.writeBytes(tmpCmd+"\n");
            }
            os.writeBytes("exit\n");
            os.flush();

            String out;
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while ((out = br.readLine()) != null) {
                Log.d("[Output]", out);
                output.add(out);
            }
            br.close();
            br = new BufferedReader(new InputStreamReader(stderr));
            while ((error = br.readLine()) != null) {
                Log.e("[Error]", error);
            }
            br.close();
            p.waitFor();
            p.destroy();
        }catch(Exception e){


        }
        return output.toArray(new String[output.size()]);
    }
}
