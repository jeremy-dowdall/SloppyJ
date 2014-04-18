/*
 * Copyright (C) 2014 Jeremy Dowdall <jeremyd@aspencloud.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fm.strength.sloppyj;

import java.util.Map;

import org.json.JSONObject;

import com.google.gson.Gson;

public class PerformanceTrials {

    private static final int cycles = 1_000_000;
    
    public static void main(String[] args) {
		PerformanceTrials trials = new PerformanceTrials();
		try {
			trials.json_to_map_1();
			trials.json_to_map_2();
			trials.json_to_map_3();
			trials.json_to_map_4();
		} catch(Exception e) {
			// exit
		}
	}

    
    private static void run(String msg, Runner runner) throws Exception {
        runner.run();
        long start = System.currentTimeMillis();
        for(int i = 0; i < cycles; i++) {
            runner.run();
        }
        long end = System.currentTimeMillis();
        System.out.println("  " + msg + (end - start));
    }

    public void json_to_map_1() throws Exception {
        final String json = "{}";

        System.out.println("json_to_map_1");
        run("Gson:       ", new Runner() { public void run() { new Gson().fromJson(json, Map.class); } });
        run("JSONObject: ", new Runner() { public void run() throws Exception { new JSONObject(json); } });
        run("SloppyJ:    ", new Runner() { public void run() { Jay.get(json).asMap(); } });
        System.out.println();
    }

    public void json_to_map_2() throws Exception {
        final String json = "{\"a\":{\"b\":\"c\"},\"d\":[{},{\"bob\":null},\"test string\"]}";

        System.out.println("json_to_map_2");
        run("Gson:       ", new Runner() { public void run() { new Gson().fromJson(json, Map.class); } });
        run("JSONObject: ", new Runner() { public void run() throws Exception { new JSONObject(json); } });
        run("SloppyJ:    ", new Runner() { public void run() { Jay.get(json).asMap(); } });
        System.out.println();
    }

    public void json_to_map_3() throws Exception {
        final String json = "{\"a\":{\"b\":\"c\"},\"d\":[{},{\"bob\":null},\"test string\"]}";
        final String sloppy = "{a:{b:c},d:[{},{bob:null},'test string'}";

        System.out.println("json_to_map_3");
        run("Gson:       ", new Runner() { public void run() { new Gson().fromJson(json, Map.class); } });
        run("JSONObject: ", new Runner() { public void run() throws Exception { new JSONObject(json); } });
        run("SloppyJ:    ", new Runner() { public void run() { Jay.get(sloppy).asMap(); } });
        System.out.println();
    }

    public void json_to_map_4() throws Exception {
        final String json = "{\"a\":{\"b\":\"c\"},\"d\":[{},{\"bob\":null},\"test string\"]}";
        final String sloppy = "{'a':{'b':'c'},'d':[{},{'bob':null},'test string'}";

        System.out.println("json_to_map_4");
        run("Gson:       ", new Runner() { public void run() { new Gson().fromJson(json, Map.class); } });
        run("JSONObject: ", new Runner() { public void run() throws Exception { new JSONObject(json); } });
        run("SloppyJ:    ", new Runner() { public void run() { Jay.get(sloppy).asMap(); } });
        System.out.println();
    }

    
    public static class Runner {
    	public void run() throws Exception {
    		
    	}
    }
}
