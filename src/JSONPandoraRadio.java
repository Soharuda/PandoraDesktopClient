import com.google.gson.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class JSONPandoraRadio
    implements PandoraRadio
{

    public JSONPandoraRadio()
    {
        incompat = false;
    }

    public String pandoraEncrypt(String s)
    {
        try
        {
            Cipher cipher = Cipher.getInstance("Blowfish/ECB/PKCS5Padding");
            java.security.Key key = new SecretKeySpec("6#26FRL$ZWD".getBytes(), "Blowfish");
            cipher.init(1, key);
            byte enc_bytes[] = cipher.doFinal(s.getBytes());
            return convertToHexString(enc_bytes);
        }
        catch(Exception e)
        {
            System.err.println("Encryption failed");
        }
        return null;
    }

    private String convertToHexString(byte input[])
    {
        StringBuffer sb = new StringBuffer(input.length);
        for(int i = 0; i < input.length; i++)
            sb.append(String.format("%02x", new Object[] {
                Byte.valueOf(input[i])
            }));

        return sb.toString();
    }

    private byte[] convertHexStringToBytes(String s)
    {
        int len = s.length();
        byte data[] = new byte[len / 2];
        for(int i = 0; i < len; i += 2)
            data[i / 2] = (byte)((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));

        return data;
    }

    public String pandoraDecrypt(String hex)
    {
        try
        {
            Cipher dec_cipher = Cipher.getInstance("Blowfish/ECB/PKCS5Padding");
            java.security.Key dec_key = new SecretKeySpec("R=U!LH$O2B#".getBytes(), "Blowfish");
            dec_cipher.init(2, dec_key);
            byte enc_bytes[] = convertHexStringToBytes(hex);
            byte dec_result[] = dec_cipher.doFinal(enc_bytes);
            byte cut[] = new byte[dec_result.length - 4];
            System.arraycopy(dec_result, 4, cut, 0, cut.length);
            return new String(cut);
        }
        catch(Exception e)
        {
            System.err.println("Decryption failed");
        }
        return null;
    }

    public void connect(String user, String password)
    {
        clientStartTime = Long.valueOf(System.currentTimeMillis() / 1000L);
        partnerLogin();
        login(user, password);
    }

    private void partnerLogin()
    {
        JsonElement partnerLoginData = doPartnerLogin();
        JsonObject asJsonObject = partnerLoginData.getAsJsonObject();
        checkForError(asJsonObject, "Failed at Partner Login");
        JsonObject result = asJsonObject.getAsJsonObject("result");
        String encryptedSyncTime = result.get("syncTime").getAsString();
        partnerAuthToken = result.get("partnerAuthToken").getAsString();
        syncTime = Long.valueOf(pandoraDecrypt(encryptedSyncTime));
        partnerId = Integer.valueOf(result.get("partnerId").getAsInt());
    }

    private void checkForError(JsonObject songResult, String errorMessage)
    {
        String stat = songResult.get("stat").getAsString();
        if(!"ok".equals(stat))
            throw new Error(errorMessage);
        else
            return;
    }

    private boolean hasError(JsonObject songResult)
    {
        String stat = songResult.get("stat").getAsString();
        return !"ok".equals(stat);
    }

    private JsonElement doPartnerLogin()
    {
        String partnerLoginUrl = "https://tuner.pandora.com/services/json/?method=auth.partnerLogin";
        Map data = new HashMap();
        data.put("username", "android");
        data.put("password", "AC7IBG09A3DTSYM4R41UJWL07VLN8JI7");
        data.put("deviceModel", "android-generic");
        data.put("version", "5");
        data.put("includeUrls", Boolean.valueOf(true));
        String stringData = (new Gson()).toJson(data);
        return doPost(partnerLoginUrl, stringData);
    }

    private JsonElement doPost(String urlInput, String stringData)
    {
        try
        {
            URL url = new URL(urlInput);
            HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            setRequestHeaders(urlConnection);
            urlConnection.setRequestProperty("Content-length", String.valueOf(stringData.length()));
            urlConnection.connect();
            DataOutputStream out = new DataOutputStream(urlConnection.getOutputStream());
            out.writeBytes(stringData);
            out.flush();
            out.close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;
            if((line = reader.readLine()) != null)
            {
                //System.out.println((new StringBuilder("response = ")).append(line).toString());
                JsonParser parser = new JsonParser();
                return parser.parse(line);
            }
        }
        catch(IOException e)
        {
            throw new Error("Failed to send POST data to Pandora");
        }
        return null;
    }

    private void setRequestHeaders(HttpURLConnection conn)
    {
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)");
        conn.setRequestProperty("Content-Type", "text/plain");
        conn.setRequestProperty("Accept", "*/*");
    }

    private long getPandoraTime()
    {
        long diff = System.currentTimeMillis() / 1000L - clientStartTime.longValue();
        return syncTime.longValue() + diff;
    }
	
	public void createStationTest() {
		Map createStationInputs = new HashMap();
		createStationInputs.put("searchText", "T-Pain");
		//createStationInputs.put("includeNearMatches", "true");
		//createStationInputs.put("includeGenreStations", "true");
		String createStationData = (new Gson()).toJson(createStationInputs);
		String encryptedStationData = pandoraEncrypt(createStationData);
		String urlEncodedPartnerAuthToken = urlEncode(partnerAuthToken);
		String createStationURL = String.format("https://tuner.pandora.com/services/json/?method=music.search&auth_token=%s&partner_id=%d", new Object[] {
            urlEncodedPartnerAuthToken, partnerId
        });
		JsonObject jsonElement = doPost(createStationURL, encryptedStationData).getAsJsonObject();
		String loginStatus = jsonElement.toString();
		System.out.println("test: "+  loginStatus);
	}

    private boolean login(String user, String password)
    {
        Map userLoginInputs = new HashMap();
        userLoginInputs.put("loginType", "user");
        userLoginInputs.put("username", user);
        userLoginInputs.put("password", password);
        userLoginInputs.put("partnerAuthToken", partnerAuthToken);
        userLoginInputs.put("syncTime", Long.valueOf(getPandoraTime()));
        String userLoginData = (new Gson()).toJson(userLoginInputs);
        String encryptedUserLoginData = pandoraEncrypt(userLoginData);
        String urlEncodedPartnerAuthToken = urlEncode(partnerAuthToken);
        String userLoginUrl = String.format("https://tuner.pandora.com/services/json/?method=auth.userLogin&auth_token=%s&partner_id=%d", new Object[] {
            urlEncodedPartnerAuthToken, partnerId
        });
        JsonObject jsonElement = doPost(userLoginUrl, encryptedUserLoginData).getAsJsonObject();
        String loginStatus = jsonElement.get("stat").getAsString();
        if("ok".equals(loginStatus))
        {
            JsonObject userLoginResult = jsonElement.get("result").getAsJsonObject();
            userAuthToken = userLoginResult.get("userAuthToken").getAsString();
            userId = Long.valueOf(userLoginResult.get("userId").getAsLong());
            return true;
        } else
        {
            return false;
        }
    }

    private String urlEncode(String s)
    {
        String encoding = "ISO-8859-1";
        try
        {
            return URLEncoder.encode(s, encoding);
        }
        catch(UnsupportedEncodingException e)
        {
            throw new RuntimeException((new StringBuilder(String.valueOf(encoding))).append(" is NOT a supported encoding").toString(), e);
        }
    }

    public void sync()
    {
    }

    public void disconnect()
    {
        syncTime = null;
        clientStartTime = null;
        partnerId = null;
        partnerAuthToken = null;
        userAuthToken = null;
        stations = null;
    }

    public ArrayList getStations()
    {
        JsonObject result = doStandardCall("user.getStationList", new HashMap(), false);
        checkForError(result, "Failed to get Stations");
        JsonArray stationArray = result.get("result").getAsJsonObject().getAsJsonArray("stations");
        stations = new ArrayList();
        HashMap hm;
        for(Iterator iterator = stationArray.iterator(); iterator.hasNext(); stations.add(new Station(hm, this)))
        {
            JsonElement jsonStationElement = (JsonElement)iterator.next();
            JsonObject jsonStation = jsonStationElement.getAsJsonObject();
            String stationId = jsonStation.get("stationId").getAsString();
            String stationIdToken = jsonStation.get("stationToken").getAsString();
            boolean isQuickMix = jsonStation.getAsJsonPrimitive("isQuickMix").getAsBoolean();
            String stationName = jsonStation.get("stationName").getAsString();
            hm = new HashMap(10);
            hm.put("stationId", stationId);
            hm.put("stationIdToken", stationIdToken);
            hm.put("isQuickMix", Boolean.valueOf(isQuickMix));
            hm.put("stationName", stationName);
        }

        Collections.sort(stations);
        return stations;
    }

    private JsonObject doStandardCall(String method, Map postData, boolean useSsl)
    {
        String url = String.format((new StringBuilder(String.valueOf(useSsl ? "https://tuner.pandora.com/services/json/?" : "http://tuner.pandora.com/services/json/?"))).append("method=%s&auth_token=%s&partner_id=%d&user_id=%s").toString(), new Object[] {
            method, urlEncode(userAuthToken), partnerId, userId
        });
        //System.out.println((new StringBuilder("url = ")).append(url).toString());
        postData.put("userAuthToken", userAuthToken);
        postData.put("syncTime", Long.valueOf(getPandoraTime()));
        String jsonData = (new Gson()).toJson(postData);
        //System.out.println((new StringBuilder("jsonData = ")).append(jsonData).toString());
        return doPost(url, pandoraEncrypt(jsonData)).getAsJsonObject();
    }

    public Station getStationById(long sid)
    {
        if(stations == null)
            getStations();
        for(Iterator iterator = stations.iterator(); iterator.hasNext();)
        {
            Station station = (Station)iterator.next();
            if(sid == station.getId())
                return station;
        }

        return null;
    }

    public boolean rate(Station station, Song song, boolean rating)
    {
        String method = "station.addFeedback";
        Map data = new HashMap();
        data.put("trackToken", song.getTrackToken());
        data.put("isPositive", Boolean.valueOf(rating));
        JsonObject ratingResult = doStandardCall(method, data, false);
        checkForError(ratingResult, "failed to rate song");
		System.out.println("rating" + ratingResult);
        return true;
    }

    public Song[] getPlaylist(Station station)
    {
        Map data = new HashMap();
        data.put("stationToken", station.getStationIdToken());
        data.put("additionalAudioUrl", "HTTP_192_MP3,HTTP_128_MP3");
        JsonObject songResult = doStandardCall("station.getPlaylist", data, true);
        if(hasError(songResult))
        {
            String err = (new StringBuilder("An error occured while getting playlist on station ")).append(station.getName()).toString();
            System.err.println((new StringBuilder("An error occured while getting playlist on station ")).append(station.getName()).toString());
            throw new RuntimeException(err);
        }
        JsonArray songsArray = songResult.get("result").getAsJsonObject().get("items").getAsJsonArray();
        List results = new ArrayList();
        String err;
        try
        {
            for(Iterator iterator = songsArray.iterator(); iterator.hasNext();)
            {
                JsonElement songElement = (JsonElement)iterator.next();
                JsonObject songData = songElement.getAsJsonObject();
                if(songData.get("adToken") == null)
                {
                    String album = songData.get("albumName").getAsString();
                    String artist = songData.get("artistName").getAsString();
                    String audioUrl = songData.get("audioUrlMap").getAsJsonObject().get("highQuality").getAsJsonObject().get("audioUrl").getAsString();
                    String additional_audioUrl = songData.get("additionalAudioUrl").getAsString();
                    if(additional_audioUrl != null)
                        audioUrl = additional_audioUrl;
                    String title = songData.get("songName").getAsString();
                    String albumDetailUrl = songData.get("albumDetailUrl").getAsString();
                    String artRadio = songData.get("albumArtUrl").getAsString();
                    String trackToken = songData.get("trackToken").getAsString();
                    Integer rating = Integer.valueOf(songData.get("songRating").getAsInt());
                    String stationId = station.getStationId();
                    results.add(new Song(album, artist, audioUrl, title, albumDetailUrl, artRadio, trackToken, rating, stationId));
                }
            }

            return (Song[])results.toArray(new Song[results.size()]);
        }
        catch(Exception e)
        {
            err = (new StringBuilder("An error occured while loading station ")).append(station.getName()).append(". Stack-trace :\n").append(e.getMessage()).toString();
        }
        throw new RuntimeException(err);
    }

    public boolean bookmarkSong(Station station, Song song)
    {
        return false;
    }

    public boolean isAlive()
    {
        return userAuthToken != null;
    }

    public boolean bookmarkArtist(Station station, Song song)
    {
        return false;
    }

    public boolean tired(Station station, Song song)
    {
        return false;
    }

    /*public String test()
    {
        String output = "";
        char passwd[] = "pandora_columbia@yahoo.com".toCharArray();
        connect("pandora_columbia@yahoo.com", new String(passwd));
        getStations();
        Station station = null;
        Iterator stationIter = stations.iterator();
        int count = 1;
        Station my_station;
        for(Iterator iterator = stations.iterator(); iterator.hasNext(); System.out.printf("%d) %s\n", new Object[] {
    Integer.valueOf(count++), my_station.getName()
}))
            my_station = (Station)iterator.next();

        station = (Station)stations.get(2);
        Song song = station.getPlaylist("mp3-hifi")[0];
        System.out.println(song.getTitle());
        output = (new StringBuilder(String.valueOf(output))).append(song.getTitle()).append(" -> ").append(song.getAudioUrl()).append("\n").toString();
        boolean rating = false;
        System.out.println("Either it worked or NOT");
        System.out.println((new StringBuilder("Is the radio alive?")).append(isAlive()).toString());
        System.out.println(output);
        return (new StringBuilder("test() method success\n")).append(output).toString();
    }*/

    public String logintest()
    {
        String output = "";
        char passwd[] = "pandora_columbia@yahoo.com".toCharArray();
        connect("pandora_columbia@yahoo.com", new String(passwd));
        return (new StringBuilder("test() method success\n")).append(output).toString();
    }

    //public static void main(String args[])
    //{
    //    (new JSONPandoraRadio()).test();
    //}

    private static final String ANDROID_DECRYPTION_KEY = "R=U!LH$O2B#";
    private static final String ANDROID_ENCRYPTION_KEY = "6#26FRL$ZWD";
    private static final String BLOWFISH_ECB_PKCS5_PADDING = "Blowfish/ECB/PKCS5Padding";
    private static final String BASE_URL = "https://tuner.pandora.com/services/json/?";
    private static final String BASE_NON_TLS_URL = "http://tuner.pandora.com/services/json/?";
    private static final String ANDROID_PARTNER_PASSWORD = "AC7IBG09A3DTSYM4R41UJWL07VLN8JI7";
    private Long syncTime;
    private Long clientStartTime;
    private Integer partnerId;
    private String partnerAuthToken;
    public String userAuthToken;
    private Long userId;
    private ArrayList stations;
    public boolean incompat;
    public static final String DEFAULT_AUDIO_FORMAT = "aacplus";
    public static final long PLAYLIST_VALIDITY_TIME = 10800L;
}
