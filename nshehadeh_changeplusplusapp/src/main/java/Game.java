import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Game {

    private static List<JsonObject> getTweets(String username) {
        HttpClient httpclient = new DefaultHttpClient();


        HttpUriRequest request = RequestBuilder.get()
            .setUri(String.format("https://api.twitter.com/1.1/statuses/user_timeline.json?screen_name=%s&count=3200&exclude_replies=true&exclude_rts=false", username))
            .setHeader("Authorization", "Bearer AAAAAAAAAAAAAAAAAAAAAOQNIAEAAAAAAagTFjwxaPdgIVNI7nM7xfNKjXI%3DFVsbBUqg9tmXC3k2BQUpWvuNlNnTWgh203HS2YufRnLagj87MT")
            .build();

        final ArrayList<JsonObject> jsonObjects = new ArrayList<>();
        try {
            HttpResponse execute = httpclient.execute(request);
            for (JsonElement jsonElement : JsonParser.parseString(IOUtils.toString(execute.getEntity().getContent(), Charset.defaultCharset())).getAsJsonArray()) {
                final JsonObject asJsonObject = jsonElement.getAsJsonObject();
                final String text = asJsonObject.get("text").getAsString();
                if (!text.contains("https://t.co"))
                    jsonObjects.add(asJsonObject);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could not contact twitter api: " + e.getLocalizedMessage());
        }
        return jsonObjects;
    }

    public static void main(String[] args) {

        List<JsonObject> potentialTweets = new ArrayList<>(getTweets("elonmusk"));
        potentialTweets.addAll(getTweets("kanyewest"));

        Random random = new Random();
        Scanner scanner = new Scanner(System.in);
        int correct = 0;
        int wrong = 0;
        System.out.println("Welcome to get the idiot. Type the person you think tweeted the following phrases. Stop and view your score at any time with \"stop\"");
        while (true) {
            final JsonObject jsonObject = potentialTweets.get(random.nextInt(potentialTweets.size()));
            int result = getAnswer(jsonObject, scanner);
            boolean wasCorrect = false;
            if (result == 0) {
                break;
            } else {
                System.out.println("Result: " + result);
                wasCorrect = result == 1;
            }
            if (wasCorrect) {
                correct++;
            } else wrong++;
            System.out.println("You were " + (wasCorrect ? "Correct" : "Not correct"));
        }
        System.out.println("Correct answers: " + correct);
        System.out.println("Incorrect answers: " + wrong);
        System.out.println("% right: " + ((correct) / ((double) (correct + wrong) * 100D)));
    }


    /**
     * Prompts the user for input and determines if they are right
     *
     * @param jsonObject source tweet
     * @param scanner    scanner to read input from
     * @return 0 if program should stop, 1 if they are correct, 2 if they are incorrect
     */
    private static int getAnswer(JsonObject jsonObject, Scanner scanner) {
        System.out.println("Tweet: " + jsonObject.get("text").getAsString());
        System.out.println("Who is this by? (Enter Kanye or Elon): ");

        String line = scanner.nextLine().trim();
        if (line.equalsIgnoreCase("stop")) {
            return 0;
        } else if (line.equalsIgnoreCase("kanye") || line.equalsIgnoreCase("elon")) {
            final String realUser = jsonObject.get("user").getAsJsonObject().get("screen_name").getAsString();
            return (line.equalsIgnoreCase("kanye") && realUser.equalsIgnoreCase("kanyewest")) || (line.equalsIgnoreCase("elon") && realUser.equalsIgnoreCase("elonmusk")) ? 1 : 2;
        } else {
            System.out.println(line + " is not kanye or elon. Can you do better? ");
            return getAnswer(jsonObject, scanner);
        }
    }
}
