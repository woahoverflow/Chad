package org.woahoverflow.chad.handle;

import sx.blah.discord.api.IDiscordClient;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@SuppressWarnings("SameReturnValue")
public class Util
{
    private static final String USER_AGENT = "Mozilla/5.0";

    public static String getTimeStamp()
    {
        return new SimpleDateFormat("MM/dd/yyyy hh:mm").format(Calendar.getInstance().getTime());
    }

    public static void sendToAdminGuild(IDiscordClient cli, String s)
    {
        cli.getGuildByID(Long.parseLong("504413906471485460")).getChannelByID(Long.parseLong("507686267216068608")).sendMessage(s);
    }

    public static String httpGet(String url) {
        try {
            URL obj = new URL(url);
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
            // optional default is GET
            con.setRequestMethod("GET");
            //add request header
            con.setRequestProperty("User-Agent", USER_AGENT);
            int responseCode = con.getResponseCode();
            if (responseCode != 200)
            {
                System.out.println("\nThere was an error sending a request to url : " + url);
                System.out.println("Response Code : " + responseCode);
            }
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static String fixEnumString(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    /*public static String replaceLast(String input, String regex, String replacement) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        if (!matcher.find()) {
            return input;
        }
        int lastMatchStart=0;
        do {
            lastMatchStart=matcher.start();
        } while (matcher.find());
        matcher.find(lastMatchStart);
        StringBuffer sb = new StringBuffer(input.length());
        matcher.appendReplacement(sb, replacement);
        matcher.appendTail(sb);
        return sb.toString();
    }*/
}