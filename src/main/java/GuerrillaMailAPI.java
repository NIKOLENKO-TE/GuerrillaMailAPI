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

  public static String getSidToken() {
    return sidToken;
  }

  public static void setSidToken(String sidToken) {
    GuerrillaMailAPI.sidToken = sidToken;
  }

  /**
   * Retrieves a new email address from the Guerrilla Mail API.
   * This method constructs a request to the Guerrilla Mail API to obtain a fresh email address.
   * It parses the response to extract the email address and prints it to the console.
   * If the API call fails or the response does not contain an email address, an error message is printed.
   *
   * @return The newly obtained email address as a String if successful, null otherwise.
   */
  public static String getEmailAddress() {
    String apiUrl = API_URL + "?f=get_email_address&lang=en&sid_token=" + sidToken;
    try {
      HttpURLConnection connection = setupConnection(apiUrl);
      String response = readResponse(connection);
      JSONObject jsonResponse = new JSONObject(response);
      //    System.out.println(jsonResponse.toString(2));
      setSidToken(jsonResponse.getString("sid_token"));
      if (jsonResponse.has("email_addr")) {
        String emailAddress = jsonResponse.getString("email_addr");
        System.out.println("\033[35m" + "Email Address: " + "\033[36m" + emailAddress + "\033[0m");
        return emailAddress;
      } else {
        System.err.println("Failed to get email address: " + response);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }


  private static String getUpdatedSidToken() {
    // Замените это на реальную логику получения актуального sid_token
    // Возможно, вам нужно будет вызвать метод или API для получения нового токена
    // Здесь это просто заглушка
    return sidToken;
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
  public static void readMail(String emailAddress, int startDelay, int numAttempts, int intervalAttempts, String stopDomain) {
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
          System.out.println("******************** " + "\033[35m" + "NEW Emails in box: [" + emailList.length() + "] " + "\033[0m" + "********************");
          for (int i = 0; i < emailList.length(); i++) {
            JSONObject emailItem = emailList.getJSONObject(i);
            System.out.println("********************** " + "\033[35m" + "Message " + (i + 1) + " of " + emailList.length() + " " + "\033[0m" + "********************** ");
            System.out.println("\033[31m" + "Email ID: " + "\033[0m" + "[" + +emailItem.getInt("mail_id") + "]");
            System.out.println("\033[37m" + "Timestamp: " + "\033[0m" + "[" + jsonResponse.getLong("ts") + "]");

            // Обновляем sid_token перед созданием ссылки
            String updatedSidToken = getUpdatedSidToken();
            printAttachmentLink(emailItem);

            System.out.println("\033[37m" + "From: " + "\033[0m" + "[" + emailItem.getString("mail_from") + "]");
            System.out.println("\033[37m" + "Subject: " + "\033[0m" + "[" + emailItem.getString("mail_subject") + "]");
            System.out.println("\033[37m" + "Message:  [" + "\n" + "\033[34m" + getEmailContent(emailItem.getInt("mail_id")) + "\033[0m");
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
//        // Вывод информации о вложениях, если они есть
//        if (jsonResponse.has("att") && jsonResponse.getInt("att") > 0) {
//          JSONArray attInfoArray = jsonResponse.getJSONArray("att_info");
//          System.out.println("Attachments:");
//          for (int i = 0; i < attInfoArray.length(); i++) {
//            JSONObject attInfo = attInfoArray.getJSONObject(i);
//            System.out.println("Type: " + attInfo.getString("t") + ", File: " + attInfo.getString("f") + ", Part ID: " + attInfo.getString("p"));
//          }
//        }
        return extractTextFromHtml(mailBody); // Extract text from HTML
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
      System.out.println("\033[35m" + "API_URL: " + "\033[0m" + apiUrl);

      System.out.println("\033[35m" + "HTTP version: " + "\033[0m" + connection.getHeaderField(0) + "\033[0m");
      System.out.println("\033[35m" + "HTTP date: " + "\033[0m" + connection.getHeaderField(1) + "\033[0m");
      System.out.println("\033[35m" + "HTTP format: " + "\033[0m" + connection.getHeaderField(2) + "\033[0m");
      System.out.println("\033[35m" + "HTTP Transfer-Encoding: " + "\033[0m" + connection.getHeaderField(3) + "\033[0m");
      System.out.println("\033[35m" + "HTTP connection: " + "\033[0m" + connection.getHeaderField(4) + "\033[0m");
      System.out.println("\033[35m" + "HTTP cache: " + "\033[0m" + connection.getHeaderField(9) + "\033[0m");
      System.out.println("\033[35m" + "HTTP Request Method: " + "\033[0m" + connection.getRequestMethod() + "\033[0m");
      System.out.println("\033[35m" + "HTTP Status Code: " + "\033[0m" + connection.getResponseCode() + "\033[0m");
      System.out.println("\033[35m" + "HTTP Message: " + "\033[0m" + connection.getResponseMessage() + "\033[0m");
      System.out.println("\033[35m" + "HTTP error code: " + "\033[0m" + connection.getErrorStream() + "\033[0m");
      JSONObject jsonObject = new JSONObject(jsonResponse);
      if (jsonObject.has("alias")) {
        String alias = jsonObject.getString("alias");
        System.out.println("\033[35m" + "Alias: " + "\033[0m" + alias);
      }
      if (jsonObject.has("ts")) {
        long ts = jsonObject.getLong("ts");
        System.out.println("\033[35m" + "TS: " + "\033[0m" + ts);
      }
      if (jsonObject.has("auth")) {
        JSONObject auth = jsonObject.getJSONObject("auth");
        System.out.println("\033[35m" + "Auth: " + "\033[0m" + auth.toString());
      }
      if (jsonObject.has("sid_token")) {
        String sidToken = jsonObject.getString("sid_token");
        System.out.println("\033[35m" + "SID Token: " + "\033[0m" + sidToken);
      }
      if (jsonObject.has("ref_mid")) {
        String ref_mid = jsonObject.getString("ref_mid");
        System.out.println("\033[35m" + "ref_mid: " + "\033[0m" + ref_mid.toString());
      }
      if (jsonObject.has("size")) {
        String size = jsonObject.getString("size");
        System.out.println("\033[35m" + "size: " + "\033[0m" + size.toString());
      }
    } catch (JSONException | IOException e) {
      e.printStackTrace();
    }
    // Assuming PHPSESSID is a static field or can be accessed from this context
    System.out.println("\033[35m" + "PHPSESSID: " + "\033[0m" + PHPSESSID);
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
   * Deletes an email address from the Guerrilla Mail API.
   * This method constructs a request to the Guerrilla Mail API to delete the specified email address.
   * It parses the response to check if the deletion was successful.
   * If the API call fails or the response indicates an error, an error message is printed.
   *
   * @param emailAddress The email address to be deleted.
   * @return True if the deletion was successful, false otherwise.
   */
  public static boolean deleteEmailAddress(String emailAddress) {
    String apiUrl = API_URL + "?f=forget_me&email_addr=" + emailAddress;
    try {
      HttpURLConnection connection = setupConnection(apiUrl);
      String response = readResponse(connection);

      // Check for API errors (e.g., HTTP status code)
      if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
        System.err.println("API Error: " + connection.getResponseCode() + " - " + connection.getResponseMessage());
        return false;
      }

      // Check for "true" response
      if (response.equals("true")) {
        System.out.println("\033[32m" + "Email address deleted successfully: " + emailAddress + "\033[0m");
        return true;
      } else {
        System.err.println("Failed to delete email address: " + response);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }


  public static void readMailNewUser(String emailUser, int startDelay, int numAttempts, int intervalAttempts, String stopDomain) {
    try {
      // Регистрация нового имени пользователя
      String setEmailUserUrl = API_URL + "?f=set_email_user&email_user=" + emailUser.split("@")[0] + "&lang=en";

      HttpURLConnection connection = setupConnection(setEmailUserUrl);
      String response = readResponse(connection);
      JSONObject jsonResponse = new JSONObject(response);
      String emailAddress = jsonResponse.getString("email_addr");
      String sidToken = jsonResponse.getString("sid_token");
      setSidToken(sidToken);

      System.out.println("\033[35m" + "Registered Email Address: " + "\033[36m" + emailAddress + "\033[0m");
      printSelectedFieldsFromResponse(response, setEmailUserUrl, connection);
      // Pause before starting mail checking
      Thread.sleep(startDelay * 1000L);

      // Mail Validation
      for (int attempt = 1; attempt <= numAttempts; attempt++) {
        System.out.println("\033[33m" + "Attempt [" + attempt + "] to check emails for [" + emailAddress + "]" + "\033[0m");
        String checkEmailUrl = API_URL + "?f=check_email&seq=0&sid_token=" + sidToken;
        connection = setupConnection(checkEmailUrl);
        response = readResponse(connection);
        jsonResponse = new JSONObject(response);
        JSONArray emailList = jsonResponse.getJSONArray("list");
        if (emailList.length() < 1) {
          System.out.println("\033[36m" + "No new emails found" + "\033[0m");
        } else {
          System.out.println("Received " + emailList.length() + " emails.");
        }
// TODO: Print the response for debugging
//        System.out.println("Response: " + jsonResponse.toString(2));
//        System.out.println(emailList);
        for (int i = 0; i < emailList.length(); i++) {
          JSONObject email = emailList.getJSONObject(i);
          System.out.println("********************** " + "\033[35m" + "Message " + (i + 1) + " of " + emailList.length() + " " + "\033[0m" + "********************** ");
          System.out.println("\033[31m" + "Email ID: " + "\033[0m" + "[" + email.getInt("mail_id") + "]");
          System.out.println("\033[37m" + "Timestamp: " + "\033[0m" + email.getLong("mail_timestamp"));

          // Check for attachment
          if (email.has("att") && email.getLong("att") > 0) {
            System.out.println("\033[37m" + "Attachment: " + "\033[0m" + "[" + email.getLong("att") + "]");
            printAttachmentLink(email); // Call method to print attachment link
          }
          System.out.println("\033[37m" + "From: " + "\033[0m" + "[" + email.getString("mail_from") + "]");
          System.out.println("\033[37m" + "Subject: " + "\033[0m" + "[" + email.getString("mail_subject") + "]");
          String emailContent = getEmailContent(email.getInt("mail_id"));
          System.out.println("\033[37m" + "Message: \n" + "\033[34m" + emailContent + "\033[0m");
          if (email.getString("mail_from").endsWith(stopDomain)) {
            System.out.println("Email from " + stopDomain + " received. Stopping email check.");
            return;
          }
        }

        if (attempt < numAttempts) {
          Thread.sleep(intervalAttempts * 1000L);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void printAttachmentLink2(JSONObject email) {
    int emailId = email.getInt("mail_id");
    String apiUrl = "https://www.guerrillamail.com/inbox?get_att&email_id=" + emailId + "&sid_token=" + sidToken;
    try {
      String response = getResponseFromUrl(apiUrl);
      JSONObject jsonResponse = new JSONObject(response);
      if (jsonResponse.has("mail_body")) {
        if (jsonResponse.has("att") && jsonResponse.getInt("att") > 0) {
          System.out.println("\033[37m" + "Attachments:" + "\033[0m" + " [" + jsonResponse.getInt("att") + "]");
          JSONArray attInfoArray = jsonResponse.getJSONArray("att_info");
          for (int i = 0; i < attInfoArray.length(); i++) {
            JSONObject attInfo = attInfoArray.getJSONObject(i);
            String fileType = attInfo.getString("t");
            String fileName = attInfo.getString("f");
            String partId = attInfo.getString("p");
            String attachmentUrl = "https://www.guerrillamail.com/inbox?get_att&email_id=" + emailId + "&part_id=" + partId;
            //System.out.println("Type: " + fileType + ", File: " + fileName + ", Part ID: " + partId);
            System.out.println("\033[37m" + "Download Link [" + (i + 1) + "]: " + attachmentUrl + "\033[0m" + "\033[37m" + ", File name:[" + "\033[0m" + fileName + "\033[37m" + "]" + "\033[0m");
          }
        } else {
          System.out.println("\033[37m" + "Attachments:" + "\033[0m" + " [No attachments found]");
        }
      } else {
        System.err.println("Failed to fetch email content: " + response);
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
      // System.out.println("API Response for email " + emailId + ": " + jsonResponse.toString(2)); // Логирование ответа API

      if (jsonResponse.has("mail_body")) {
        if (jsonResponse.has("att") && jsonResponse.getInt("att") > 0) {
          System.out.println("\033[37m" + "Attachments:" + "\033[0m" + " [" + jsonResponse.getInt("att") + "]");
          JSONArray attInfoArray = jsonResponse.getJSONArray("att_info");
          for (int i = 0; i < attInfoArray.length(); i++) {
            JSONObject attInfo = attInfoArray.getJSONObject(i);
            String fileType = attInfo.getString("t");
            String fileName = attInfo.getString("f");
            String partId = attInfo.getString("p");
            String attachmentUrl = "https://www.guerrillamail.com/inbox?get_att&email_id=" + emailId + "&part_id=" + partId + "&sid_token=" + getSidToken();

            // Дополнительное логирование
            //System.out.println("Attachment Info - File Type: " + fileType + ", File Name: " + fileName + ", Part ID: " + partId);
            System.out.println("\033[37m" + "Download Link " + "\033[0m" + "[" + (i + 1) + "]: " + attachmentUrl + "\033[37m" + ", File name:[" + "\033[0m" + fileName + "\033[37m" + "]" + "\033[0m");
          }
        } else {
          System.out.println("\033[37m" + "Attachments:" + "\033[0m" + " [No attachments found]");
        }
      } else {
        System.err.println("Failed to fetch email content: " + response);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  @Test
  public void test1() {
    readMail(getEmailAddress(), 10, 5, 20, "target@email.com");
  }

  @Test
  public void test2() {
    readMailNewUser("msyyppnz@guerrillamailblock.com", 1, 5, 10, "target@email.com");
  }
}
