import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class GuerrillaMailAPI {
  private static final String API_URL = "https://api.guerrillamail.com/ajax.php";
  private static String PHPSESSID = null; // Stores the PHPSESSID cookie for subsequent requests
  private static final String sidToken = null; // Placeholder for a potential SID token (not used in the current code)

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
      if (jsonResponse.has("email_addr")) {
        String emailAddress = jsonResponse.getString("email_addr");
        System.out.println("\033[36m" + "Email Address: " + emailAddress + "\033[0m");
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
  public static void readMail(String emailAddress, int startDelay, int numAttempts, int intervalAttempts, String stopDomain) {
    try {
      Thread.sleep(startDelay * 1000L); // Convert to milliseconds
      for (int attempt = 1; attempt <= numAttempts; attempt++) {
        System.out.println("Attempt " + attempt + " to check emails...");
        String apiUrl = API_URL + "?f=check_email&seq=0&email=" + emailAddress + "&sid_token=" + sidToken;
        String response = getResponseFromUrl(apiUrl);
        JSONObject jsonResponse = new JSONObject(response);
        if (jsonResponse.has("list")) {
          JSONArray emailList = jsonResponse.getJSONArray("list");
          System.out.println("******************** " + "\033[35m" + "Emails in box: [" + emailList.length() + "] " + "\033[0m" + "********************");
          for (int i = 0; i < emailList.length(); i++) {
            JSONObject emailItem = emailList.getJSONObject(i);
            System.out.println("********************** " + "\033[35m" + "Message " + (i + 1) + " of " + emailList.length() + " " + "\033[0m" + "********************** ");
            System.out.println("\033[31m" + "Email ID: [" + emailItem.getInt("mail_id") + "]" + "\033[0m");
            System.out.println("From: [" + emailItem.getString("mail_from") + "]");
            System.out.println("Subject: [" + emailItem.getString("mail_subject") + "]");
            System.out.println("Message: " + "\033[34m" + getEmailContent(emailItem.getInt("mail_id")) + "\033[0m");
            if (emailItem.getString("mail_from").endsWith(stopDomain)) {
              System.out.println("Email from " + stopDomain + " received. Stopping email check.");
              return; // Exit after receiving email from specified domain
            }
          }
        } else {
          System.err.println("Attempt " + attempt + ": No emails found.");
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
      System.out.println("Response from " + apiUrl + ": " + response); // Debugging output
      return response;
    }
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

  @Test
  public void test() {
    readMail(getEmailAddress(), 1, 50, 5, "target@email.com");
  }
  public static boolean setEmailUser(String emailAddress) {
    String apiUrl = API_URL + "?f=set_email_user&email=" + emailAddress;
    try {
      String response = getResponseFromUrl(apiUrl);
      System.out.println("Response from " + apiUrl + ": " + response); // Логирование полного ответа

      // Попробуйте парсить ответ как JSON только если он выглядит как JSON
      if (response.trim().startsWith("{")) {
        JSONObject jsonResponse = new JSONObject(response);
        if (jsonResponse.has("success") && jsonResponse.getBoolean("success")) {
          System.out.println("Email user set successfully.");
          return true;
        } else {
          System.err.println("Failed to set email user: " + response);
        }
      } else {
        System.err.println("Unexpected response format: " + response);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  /**
   * Читает письма с существующего почтового ящика.
   *
   * @param emailAddress     Существующий почтовый ящик для проверки писем.
   * @param startDelay       Задержка в секундах перед первой попыткой проверки почты.
   * @param numAttempts      Количество попыток проверки писем.
   * @param intervalAttempts Интервал в секундах между попытками проверки почты.
   * @param stopDomain       Домен, с которого, если получено письмо, метод немедленно прекратит работу.
   */
  public static void readExistingMail(String emailAddress, int startDelay, int numAttempts, int intervalAttempts, String stopDomain) {
    if (emailAddress == null || emailAddress.isEmpty()) {
      System.err.println("Неверный адрес электронной почты. Не удается проверить почту.");
      return;
    }

    try {
      Thread.sleep(startDelay * 1000L); // Конвертировать в миллисекунды

      // Установить почтовый ящик (может потребоваться, если API требует этого шага)
      if (!setEmailUser(emailAddress)) {
        System.err.println("Не удалось установить почтовый ящик. Выход.");
        return;
      }

      for (int attempt = 1; attempt <= numAttempts; attempt++) {
        System.out.println("Попытка " + attempt + " проверить почту...");
        String apiUrl = API_URL + "?f=check_email&seq=0&email=" + emailAddress + "&sid_token=" + sidToken;
        String response = getResponseFromUrl(apiUrl);
        System.out.println("Ответ от API: " + response); // Логирование полного ответа для отладки

        JSONObject jsonResponse = new JSONObject(response);
        if (jsonResponse.has("list")) {
          JSONArray emailList = jsonResponse.getJSONArray("list");
          System.out.println("******************** " + "\033[35m" + "Письма в ящике: [" + emailList.length() + "] " + "\033[0m" + "********************");
          for (int i = 0; i < emailList.length(); i++) {
            JSONObject emailItem = emailList.getJSONObject(i);
            System.out.println("********************** " + "\033[35m" + "Сообщение " + (i + 1) + " из " + emailList.length() + " " + "\033[0m" + "********************** ");
            System.out.println("\033[31m" + "ID письма: [" + emailItem.getInt("mail_id") + "]" + "\033[0m");
            System.out.println("От: [" + emailItem.getString("mail_from") + "]");
            System.out.println("Тема: [" + emailItem.getString("mail_subject") + "]");
            System.out.println("Сообщение: " + "\033[34m" + getEmailContent(emailItem.getInt("mail_id")) + "\033[0m");
            if (emailItem.getString("mail_from").endsWith(stopDomain)) {
              System.out.println("Получено письмо от " + stopDomain + ". Остановка проверки почты.");
              return; // Выход после получения письма с указанного домена
            }
          }
        } else {
          System.err.println("Попытка " + attempt + ": Письма не найдены.");
        }
        if (attempt < numAttempts) {
          Thread.sleep(intervalAttempts * 1000L); // Пауза между попытками
        }
      }
      System.err.println("Все попытки проверки почты завершены.");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  public void test2() {
    readExistingMail("txlzht+2qlqv6s@grr.la", 1, 5, 5, "target@email.com");
  }
}
