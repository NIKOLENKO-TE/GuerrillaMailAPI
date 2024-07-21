import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class GuerrillaMailAPI {
  private static final String API_URL = "https://api.guerrillamail.com/ajax.php";
  private static String PHPSESSID = null; // Stores the PHPSESSID cookie for subsequent requests
  private static String sidToken = null; // Placeholder for a potential SID token (not used in the current code)
  private static final String RESET = "\033[0m";
  private static final String RED = "\033[31m";
  private static final String GREEN = "\033[32m";
  private static final String YELLOW = "\033[33m";
  private static final String BLUE = "\033[34m";
  private static final String PURPLE = "\033[35m";
  private static final String CYAN = "\033[36m";
  private static final String GRAY = "\033[37m";

  private static void print(String color, String key, String value) {
    System.out.println(color + key + ": " + RESET + "[" + value + "]");
  }

  public static String getSidToken() {
    return sidToken;
  }

  public static void setSidToken(String sidToken) {
    GuerrillaMailAPI.sidToken = sidToken;
  }
  public static String getSessionData(String emailAddress) {
    String emailUser = emailAddress.split("@")[0];
    String apiUrl = API_URL + "?f=set_email_user&email_user=" + emailUser + "&lang=en";
    try {
      HttpURLConnection connection = setupConnection(apiUrl);
      String response = readResponse(connection);
      JSONObject jsonResponse = new JSONObject(response);
      setSidToken(jsonResponse.getString("sid_token"));
      System.out.println(PURPLE + "Session Data Received for Email: " + CYAN + emailAddress + RESET);
      return jsonResponse.getString("sid_token");
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
  /**
   * Retrieves a new email address from the Guerrilla Mail API.
   * This method constructs a request to the Guerrilla Mail API to obtain a fresh email address.
   * It parses the response to extract the email address and prints it to the console.
   * If the API call fails or the response does not contain an email address, an error message is printed.
   *
   * @return The newly obtained email address as a String if successful, null otherwise.
   */
  public static String getRandomEmailAddress() {
    String apiUrl = API_URL + "?f=get_email_address&lang=en&sid_token=" + sidToken;
    try {
      HttpURLConnection connection = setupConnection(apiUrl);
      String response = readResponse(connection);
      JSONObject jsonResponse = new JSONObject(response);
      //    System.out.println(jsonResponse.toString(2));
      setSidToken(jsonResponse.getString("sid_token"));
      if (jsonResponse.has("email_addr")) {
        String emailAddress = jsonResponse.getString("email_addr");
        System.out.println(PURPLE + "Email Address: " + "\033[36m" + emailAddress + RESET);
        return emailAddress;
      } else {
        System.err.println("Failed to get email address: " + response);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }



  /**
   * Attempts to read emails for a given email address with specified parameters and conditions.
   * This method waits for a start delay, then checks for emails at specified intervals for a number of attempts
   * or until a specified timeout is reached. Additionally, if an email from a specific domain is received,
   * the method will immediately stop checking for more emails and exit.
   *
   * @param emailAddress     The email address to check for new emails.
   * @param startDelay       The delay in seconds before the first attempt to check emails.
   * @param numAttempts      The number of attempts to check for emails.
   * @param intervalAttempts The interval in seconds between each email check attempt.
   * @param stopDomain       The domain name that, if an email is received from, will cause the method to immediately stop.
   */
  public static void readFromRandomEmail(String emailAddress, int startDelay, int numAttempts, int intervalAttempts, String stopDomain) {
    try {
      Thread.sleep(startDelay * 1000L); // Convert to milliseconds

      for (int attempt = 1; attempt <= numAttempts; attempt++) {
        System.out.println("Attempt [" + attempt + "] to check emails...");
        String apiUrl = API_URL + "?f=check_email&seq=0&email=" + emailAddress + "&sid_token=" + sidToken;
        HttpURLConnection connection = setupConnection(apiUrl);
        String response = readResponse(connection);
        printSelectedFieldsFromResponse(response, apiUrl, connection);
        JSONObject jsonResponse = new JSONObject(response);

        if (jsonResponse.has("list")) {
          JSONArray emailList = jsonResponse.getJSONArray("list");
          System.out.println("******************** " + PURPLE + "NEW Emails in box: [" + emailList.length() + "] " + RESET + "********************");
          for (int i = 0; i < emailList.length(); i++) {
            JSONObject emailItem = emailList.getJSONObject(i);
            System.out.println("********************** " + PURPLE + "Message " + (i + 1) + " of " + emailList.length() + " " + RESET + "********************** ");
            print(GRAY, "Email ID", emailItem.getInt("mail_id") + "");
            print(GRAY, "Timestamp", emailItem.getLong("mail_timestamp") + "");
            printAttachmentLink(emailItem);
            print(GRAY, "From", emailItem.getString("mail_from"));
            print(GRAY, "To", emailItem.getString("mail_recipient"));
            print(GRAY, "Subject", emailItem.getString("mail_subject"));
            System.out.println(GRAY + "Message:  [" + "\n" + "\033[34m" + getEmailContent(emailItem.getInt("mail_id")) + RESET);
            if (emailItem.getString("mail_from").endsWith(stopDomain)) {
              System.out.println("Email from " + stopDomain + " received. Stopping email check.");
              return; // Exit after receiving email from specified domain
            }
          }
        } else {
          System.err.println("Attempt " + attempt + ": No new emails found.");
        }
        if (attempt < numAttempts) {
          Thread.sleep(intervalAttempts * 1000L); // Pause between attempts
        }
      }
      System.err.println("All attempts to check emails completed.");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  /**
   * Fetches the content of an email by its ID from the Guerrilla Mail API.
   * This method constructs a request to the Guerrilla Mail API to fetch the content of a specific email by its ID.
   * It parses the response to extract the email content, which is then cleaned of HTML tags before being returned.
   * If the API call fails or the response does not contain the email content, an error message is printed.
   *
   * @param mailId The ID of the email whose content is to be fetched.
   * @return The plain text content of the email if successful, a failure message otherwise.
   */
  private static String getEmailContent(int mailId) {
    String apiUrl = API_URL + "?f=fetch_email&email_id=" + mailId + "&sid_token=" + sidToken;
    try {
      String response = getResponseFromUrl(apiUrl);
      JSONObject jsonResponse = new JSONObject(response);
      if (jsonResponse.has("mail_body")) {
        String mailBody = jsonResponse.getString("mail_body");
        // Extract text from HTML
        return extractTextFromHtml(mailBody);
      } else {
        System.err.println("Failed to fetch email content: " + response);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "Failed to fetch email content.";
  }

  /**
   * Extracts text from an HTML string by removing all HTML tags.
   * This method uses a regular expression to remove all HTML tags from the input string,
   * leaving only the plain text content.
   *
   * @param html The HTML string from which text is to be extracted.
   * @return The extracted plain text content.
   */
  private static String extractTextFromHtml(String html) {
    return html.replaceAll("\\<.*?>", "").trim(); // Remove all HTML tags and trim leading and trailing whitespace
  }

  /**
   * Sends a request to a specified URL and returns the response as a string.
   * This method sets up a connection to the given URL, sends a GET request, and reads the response.
   * It handles redirects and cookies, specifically looking for and storing the PHPSESSID cookie.
   * The response is read into a string and returned.
   *
   * @param apiUrl The URL to which the request is sent.
   * @return The response from the URL as a String.
   * @throws Exception If an error occurs during the connection or reading the response.
   */
  private static String getResponseFromUrl(String apiUrl) throws Exception {
    HttpURLConnection connection = setupConnection(apiUrl);
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
      StringBuilder responseBuilder = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        responseBuilder.append(line);
      }
      String response = responseBuilder.toString();
      return response;
    }
  }

  /**
   * Prints selected fields from a JSON response string.
   * This method parses a JSON string to extract and print specific fields: "alias", "ts", "sid_token", and "auth".
   * If any of these fields are present in the JSON object, their values are printed to the console.
   * The method uses ANSI escape codes to color the output for better readability:
   * - Purple (\033[35m) for field names.
   * - Default console color (\033[0m) for field values.
   *
   * @param jsonResponse The JSON response string to parse and from which to print the selected fields.
   */
  private static void printSelectedFieldsFromResponse(String jsonResponse, String apiUrl, HttpURLConnection connection) {
    try {
      print(PURPLE, "API_URL", apiUrl);
      print(PURPLE, "HTTP Version", connection.getHeaderField(0));
      print(PURPLE, "HTTP Date", connection.getHeaderField(1));
      print(PURPLE, "HTTP Format", connection.getHeaderField(2));
      print(PURPLE, "HTTP Transfer-Encoding", connection.getHeaderField(3));
      print(PURPLE, "HTTP Connection", connection.getHeaderField(4));
      print(PURPLE, "HTTP Request Method", connection.getRequestMethod());
      print(PURPLE, "HTTP Response Code", connection.getResponseCode() + "");
      print(PURPLE, "HTTP Response Message", connection.getResponseMessage());
      print(PURPLE, "HTTP Content-Type", connection.getContentType());
      print(PURPLE, "HTTP error code", connection.getErrorStream() + "");
      JSONObject jsonObject = new JSONObject(jsonResponse);
      if (jsonObject.has("alias")) {
        String alias = jsonObject.getString("alias");
        print(PURPLE, "Alias", alias);
      }
      if (jsonObject.has("ts")) {
        long ts = jsonObject.getLong("ts");
        print(PURPLE, "TS", ts + "");
      }
      if (jsonObject.has("auth")) {
        JSONObject auth = jsonObject.getJSONObject("auth");
        print(PURPLE, "Auth", auth.toString());
      }
      if (jsonObject.has("sid_token")) {
        String sidToken = jsonObject.getString("sid_token");
        print(PURPLE, "SID Token", sidToken);
      }
      if (jsonObject.has("ref_mid")) {
        String ref_mid = jsonObject.getString("ref_mid");
        print(PURPLE, "ref_mid", ref_mid);
      }
      if (jsonObject.has("size")) {
        print(PURPLE, "size", jsonObject.getString("size"));
      }
    } catch (JSONException | IOException e) {
      e.printStackTrace();
    }
    // Assuming PHPSESSID is a static field or can be accessed from this context
    print(PURPLE, "PHPSESSID", PHPSESSID);
  }

  /**
   * Sets up a basic connection to a specified URL.
   * This method configures the connection with necessary headers and sets up redirect handling.
   *
   * @param apiUrl The URL to which the connection is set up.
   * @return The configured HttpURLConnection.
   * @throws Exception If an error occurs during the connection setup.
   */
  private static HttpURLConnection setupBasicConnection(String apiUrl) throws Exception {
    HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
    connection.setInstanceFollowRedirects(true);
    connection.setRequestProperty("Accept", "application/json");
    connection.setRequestProperty("User-Agent", "Mozilla/5.0");
    if (PHPSESSID != null) {
      connection.setRequestProperty("Cookie", "PHPSESSID=" + PHPSESSID);
    }
    return connection;
  }

  /**
   * Handles redirects and extracts PHPSESSID cookies from the response headers.
   * This method checks for redirect responses and updates the connection accordingly.
   * It also extracts the PHPSESSID cookie from the response headers if present.
   *
   * @param connection The HttpURLConnection to handle redirects and cookies for.
   * @return The updated HttpURLConnection after handling redirects and cookies.
   * @throws Exception If an error occurs during redirect handling or cookie extraction.
   */
  private static HttpURLConnection handleRedirectsAndCookies(HttpURLConnection connection) throws Exception {
    int status = connection.getResponseCode();
    if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM) {
      String newUrl = connection.getHeaderField("Location");
      connection = setupBasicConnection(newUrl);
    }
    Map<String, List<String>> headers = connection.getHeaderFields();
    for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
      if (entry.getKey() != null && entry.getKey().equalsIgnoreCase("Set-Cookie")) {
        List<String> cookies = entry.getValue();
        for (String cookie : cookies) {
          if (cookie.startsWith("PHPSESSID=")) {
            PHPSESSID = cookie.split(";")[0].split("=")[1];
          }
        }
      }
    }
    return connection;
  }

  /**
   * Sets up a connection to a specified URL, handling redirects and cookies.
   * This method configures the connection with necessary headers and checks for PHPSESSID cookies.
   *
   * @param apiUrl The URL to which the connection is set up.
   * @return The configured HttpURLConnection.
   * @throws Exception If an error occurs during the connection setup.
   */
  private static HttpURLConnection setupConnection(String apiUrl) throws Exception {
    HttpURLConnection connection = setupBasicConnection(apiUrl);
    return handleRedirectsAndCookies(connection);
  }

  /**
   * Reads the response from a given HttpURLConnection.
   * This method reads the input stream from the connection and returns the response as a string.
   *
   * @param connection The HttpURLConnection from which the response is read.
   * @return The response from the connection as a String.
   * @throws Exception If an error occurs during reading the response.
   */
  private static String readResponse(HttpURLConnection connection) throws Exception {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
      StringBuilder responseBuilder = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        responseBuilder.append(line);
      }
      return responseBuilder.toString();
    }
  }

  /**
   * Deletes an email by its ID using the Guerrilla Mail API.
   * This method constructs a request to the Guerrilla Mail API to delete a specific email by its ID.
   * It parses the response to verify the deletion and prints a success message if the email is deleted.
   * If the API call fails or the response does not indicate a successful deletion, an error message is printed.
   *
   * @param emailId The ID of the email to be deleted.
   */
  public static void deleteEmail(int emailId, String sidToken) {
    try {
      String apiUrl = API_URL + "?f=del_email&email_ids[]=" + emailId + "&sid_token=" + sidToken;
      HttpURLConnection connection = setupConnection(apiUrl);
      String response = readResponse(connection);
      JSONObject jsonResponse = new JSONObject(response);
      if (jsonResponse.has("deleted_ids")) {
        JSONArray deletedIds = jsonResponse.getJSONArray("deleted_ids");
        if (deletedIds.length() > 0 && !"0".equals(deletedIds.getString(0))) {
          System.out.println(RED + "Email deleted successfully: " + RESET);
        } else {
          System.err.println("Failed to delete email or email ID not found: " + response);
        }
      } else {
        System.err.println("Failed to delete email!" + response);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  /**
   * Fetches the list of emails for a given email address and returns their IDs.
   *
   * @param emailAddress The email address to check for new emails.
   * @return An array of email IDs.
   */
  public static int[] getEmailIds(String emailAddress) {
    try {
      String apiUrl = API_URL + "?f=check_email&seq=0&email=" + emailAddress + "&sid_token=" + sidToken;
      HttpURLConnection connection = setupConnection(apiUrl);
      String response = readResponse(connection);
      JSONObject jsonResponse = new JSONObject(response);

      if (jsonResponse.has("list")) {
        JSONArray emailList = jsonResponse.getJSONArray("list");
        int[] emailIds = new int[emailList.length()];
        for (int i = 0; i < emailList.length(); i++) {
          JSONObject emailItem = emailList.getJSONObject(i);
          emailIds[i] = emailItem.getInt("mail_id");
        }
        return emailIds;
      } else {
        System.err.println("No emails found.");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new int[0];
  }
  public static void readFromIndividualEmail(String emailUser, int startDelay, int numAttempts, int intervalAttempts, String stopDomain) {
    try {
      // 1. Register a new email user
      String setEmailUserUrl = API_URL + "?f=set_email_user&email_user=" + emailUser.split("@")[0] + "&lang=en";

      HttpURLConnection connection = setupConnection(setEmailUserUrl);
      String response = readResponse(connection);
      JSONObject jsonResponse = new JSONObject(response);
      String emailAddress = jsonResponse.getString("email_addr");
      String sidToken = jsonResponse.getString("sid_token");
      setSidToken(sidToken);
      System.out.println(PURPLE + "Registered Email Address: " + CYAN + emailAddress + RESET);
      // 2. Print selected fields from the response
      printSelectedFieldsFromResponse(response, setEmailUserUrl, connection);

      // 3. Wait for the initial delay
      Thread.sleep(startDelay * 1000L);

      // 4. Check for emails repeatedly
      for (int attempt = 1; attempt <= numAttempts; attempt++) {
        print(YELLOW, "Attempt", attempt + "");
        String checkEmailUrl = API_URL + "?f=check_email&seq=0&sid_token=" + sidToken;
        connection = setupConnection(checkEmailUrl);
        response = readResponse(connection);
        jsonResponse = new JSONObject(response);
        JSONArray emailList = jsonResponse.getJSONArray("list");

        // 5. Process received emails
        if (emailList.length() < 1) {
          System.out.println((RED + "No new emails found" + RESET));
        } else {
          System.out.println("Received " + emailList.length() + " emails.");
        }
// TODO: Print the response for debugging
//        System.out.println("Response: " + jsonResponse.toString(2));
//        System.out.println(emailList);
        for (int i = 0; i < emailList.length(); i++) {
          JSONObject email = emailList.getJSONObject(i);
          System.out.println("********************** " + PURPLE + "Message " + (i + 1) + " of " + emailList.length() + " " + RESET + "********************** ");
          print(GRAY, "Email ID", email.getInt("mail_id") + "");
          print(GRAY, "Timestamp", email.getLong("mail_timestamp") + "");

          // 6. Check for attachment
          if (email.has("att") && email.getLong("att") > 0) {
            printAttachmentLink(email); // Call method to print attachment link
          }
          print(GRAY, "From", email.getString("mail_from"));
          print(GRAY, "Subject", email.getString("mail_subject"));
          System.out.println(GRAY + "Message:  " + RESET + "[\n" + BLUE + getEmailContent(email.getInt("mail_id")) + RESET);

          // 7. Stop if the email is from the specified domain
          if (email.getString("mail_from").endsWith(stopDomain)) {
            print(RED, "Email from", stopDomain + " received. Stopping email check.");
            return;
          }
        }

        // 8. Wait for the interval between attempts
        if (attempt < numAttempts) {
          Thread.sleep(intervalAttempts * 1000L);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  private static void printAttachmentLink(JSONObject email) {
    int emailId = email.getInt("mail_id");
    String apiUrl = API_URL + "?f=fetch_email&email_id=" + emailId + "&sid_token=" + getSidToken();
    try {
      String response = getResponseFromUrl(apiUrl);
      JSONObject jsonResponse = new JSONObject(response);
// TODO: API response logging
// System.out.println("API Response for email " + emailId + ": " + jsonResponse.toString(2));
      if (jsonResponse.has("mail_body")) {
        if (jsonResponse.has("att") && jsonResponse.getInt("att") > 0) {
          print("\033[37m", "Attachment", jsonResponse.getInt("att") + "");
          JSONArray attInfoArray = jsonResponse.getJSONArray("att_info");
          for (int i = 0; i < attInfoArray.length(); i++) {
            JSONObject attInfo = attInfoArray.getJSONObject(i);
            String fileType = attInfo.getString("t");
            String fileName = attInfo.getString("f");
            String partId = attInfo.getString("p");
            String attachmentUrl = "https://www.guerrillamail.com/inbox?get_att&email_id=" + emailId + "&part_id=" + partId + "&sid_token=" + getSidToken();
            System.out.println(GRAY + "Download Link " + RESET + "[" + (i + 1) + "]: " + attachmentUrl + GRAY + ", File name:" + RESET + "[" + fileName + "]" + RESET + GRAY + ", File type: ["+ RESET + fileType + "]");
          }
        } else {
          print(GRAY, "Attachments", "No attachments found");
        }
      } else {
        print(RED, "Failed to fetch attachment link", response);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  public static int getEmailId(String emailAddress, String sidToken) {
    try {
      // Form the URL for the request
      String apiUrl = API_URL + "?f=check_email&sid_token=" + sidToken + "&seq=20";
      // Make the HTTP request
      HttpURLConnection connection = setupConnection(apiUrl);
      String response = readResponse(connection);
      // Parse the JSON response
      JSONObject jsonResponse = new JSONObject(response);
      if (jsonResponse.has("list")) {
        JSONArray emailList = jsonResponse.getJSONArray("list");
        if (emailList.length() > 0) {
          // Extract the ID of the first email
          int emailId = emailList.getJSONObject(0).getInt("mail_id");
          //print(GREEN, "Email ID", emailId + "");
          return emailId;
        } else {
          //print(RED, "No emails found for address", emailAddress);
        }
      } else {
        print(RED, "Failed to fetch email list", response);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return -1; // Return -1 in case of error or no emails found
  }

  @Test
  public void createRandomAccount() {
    readFromRandomEmail(getRandomEmailAddress(), 1, 5, 20, "stop@email.com");
  }

  @Test
  public void createNewAccount() {
    readFromIndividualEmail("portishead@guerrillamailblock.com", 1, 5, 10, "stop@email.com");
  }

  @Test
    public void deleteAccount() {
    String emailToDelete = "portishead@guerrillamailblock.com";
    String sidTokenEmailToDelete = getSessionData(emailToDelete);
    deleteEmail(getEmailId(emailToDelete, sidTokenEmailToDelete), sidTokenEmailToDelete);
  }
}
