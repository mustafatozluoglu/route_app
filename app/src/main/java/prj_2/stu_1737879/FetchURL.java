package prj_2.stu_1737879;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FetchURL extends AsyncTask<String, Void, String> {

    private JSONObject jsonObject;

    private List<String> entries_list = new ArrayList<>();

    private String distance_text;
    private String duration_text;
    private String end_address;
    private String end_location;
    private String start_address;
    private String start_location;


    Context mContext;
    String directionMode = "driving";

    public FetchURL(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    protected String doInBackground(String... strings) {
        // For storing data from web service
        String data = "";
        directionMode = strings[1];
        try {
            // Fetching the data from web service
            data = downloadUrl(strings[0]);
            Log.d("mylog", "Background task data " + data.toString());
        } catch (Exception e) {
            Log.d("Background Task", e.toString());
        }
        parse(jsonObject);
        return data;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        PointsParser parserTask = new PointsParser(mContext, directionMode);

        try {
            jsonObject = new JSONObject(s);
            parse(jsonObject);


        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Invokes the thread for parsing the JSON data
        parserTask.execute(s);
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();
            // Connecting to url
            urlConnection.connect();
            // Reading data from url
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            Log.d("mylog", "Downloaded URL: " + data.toString());
            br.close();
        } catch (Exception e) {
            Log.d("mylog", "Exception downloading URL: " + e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    public List<String> getEntries_list() {
        return entries_list;
    }

    public List<List<HashMap<String, String>>> parse(JSONObject jObject) {

        List<List<HashMap<String, String>>> routes = new ArrayList<>();
        JSONArray jRoutes;
        JSONArray jLegs;
        JSONArray jSteps;
        try {
            jRoutes = jObject.getJSONArray("routes");
            /** Traversing all routes */
            for (int i = 0; i < jRoutes.length(); i++) {
                jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");

                List path = new ArrayList<>();
                /** Traversing all legs */
                for (int j = 0; j < jLegs.length(); j++) {
                    distance_text = String.valueOf(((JSONObject) jLegs.get(i)).getJSONObject("distance"));
                    duration_text = String.valueOf(((JSONObject) jLegs.get(i)).getJSONObject("duration"));
                    end_address = String.valueOf(((JSONObject) jLegs.get(i)).get("end_address"));
                    end_location = String.valueOf(((JSONObject) jLegs.get(i)).getJSONObject("end_location"));
                    start_address = String.valueOf(((JSONObject) jLegs.get(i)).get("start_address"));
                    start_location = String.valueOf(((JSONObject) jLegs.get(i)).getJSONObject("start_location"));

                    entries_list.add("DISTANCE: " + distance_text);
                    entries_list.add("DURATION: " + duration_text);
                    entries_list.add("START ADDRESS: " + start_address);
                    entries_list.add("START LOCATION: " + start_location);
                    entries_list.add("END ADDRESS: " + end_address);
                    entries_list.add("END LOCATION: " + end_location);

                    System.out.println("ENTRIES LIST:" + entries_list);

                    routes.add(path);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }
        return routes;
    }
}


