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

/**
 * <strong>Русский:</strong><br>
 * <code>Основной класс для взаимодействия с API Guerrilla Mail.</code><br>
 * Этот класс включает методы для получения случайного и конкретного адреса электронной почты, чтения писем,
 * удаления писем и обработки данных сеанса с сервисом Guerrilla Mail.<br>
 * </p>
 * <strong>English:</strong><br>
 * <code>Main class for interacting with the Guerrilla Mail API.</code><br>
 * This class includes methods for obtaining a random and specific email address, reading emails,
 * deleting emails, and handling session data with the Guerrilla Mail service.<br>
 * </p>
 *
 * @see <a href="https://www.guerrillamail.com/">Guerrilla Mail General Website</a>
 * @see <a href="https://www.guerrillamail.com/GuerrillaMailAPI.html#fetch_email">Guerrilla Mail API Documentation - fetch_email</a>
 */
public class GuerrillaMailAPI {

    private static final String API_URL = "https://api.guerrillamail.com/ajax.php";
    private static String PHPSESSID = null; // Нужен для хранения идентификатора сеанса PHP для доступа к API
    private static String sidToken = null; // Нужен для хранения токена сеанса для доступа к API
    private static final String RESET = "\033[0m";
    private static final String RED = "\033[31m";
    private static final String GREEN = "\033[32m";
    private static final String YELLOW = "\033[33m";
    private static final String BLUE = "\033[34m";
    private static final String PURPLE = "\033[35m";
    private static final String CYAN = "\033[36m";
    private static final String GRAY = "\033[37m";

    /**
     * <strong>Русский:</strong><br>
     * <code>Этот метод выводит ключ и его значение в консоль с заданным цветом.</code><br>
     * Он используется для форматированного вывода информации, где ключ и значение отображаются в заданном цвете.<br>
     * </p>
     * <strong>English:</strong><br>
     * <code>This method prints a key and its value to the console in the specified color.</code><br>
     * It is used for formatted output where the key and value are displayed in the specified color.<br>
     * </p>
     *
     * @param color the color to print the key and value (цвет для вывода ключа и значения)
     * @param key the key to print (ключ для вывода)
     * @param value the value to print (значение для вывода)
     */
    private static void print(String color, String key, String value) {
        System.out.println(color + key + ": " + RESET + "[" + value + "]");
    }

    public static String getSidToken() {
        return sidToken;
    }

    public static void setSidToken(String sidToken) {
        GuerrillaMailAPI.sidToken = sidToken;
    }

    /**
     * <strong>Русский:</strong><br>
     * <code>Этот метод выполняет вызов API по указанному URL и возвращает ответ в виде объекта JSON.</code><br>
     * Он устанавливает соединение с сервером, получает ответ и преобразует его в объект JSON.<br>
     * </p>
     * <strong>English:</strong><br>
     * <code>This method makes an API call to the specified URL and returns the response as a JSON object.</code><br>
     * It sets up the connection, retrieves the response, and converts it into a JSON object.<br>
     * </p>
     *
     * @param url the URL to make the API call to (URL для вызова API)
     * @return the {@link JSONObject} <code>response from the API call (JSON-ответ от вызова API)</code>
     * @throws Exception if an error occurs during the API call into a {@link JSONObject} (выбрасывается исключение, если произошла ошибка при вызове API)
     */

    private static JSONObject makeApiCall(String url) throws Exception {
        HttpURLConnection connection = setupConnection(url);
        String response = readResponse(connection);
        return new JSONObject(response);
    }

    /**
     * <p>
     * <strong>Русский:</strong><br>
     * <code>Этот метод задерживает выполнение программы на заданное количество секунд.</code>
     * </p>
     * <strong>English:</strong><br>
     * <code>This method pauses the execution of the program for the given number of seconds.</code>
     * </p>
     * @param seconds the number of seconds to delay the execution
     *               (количество секунд для задержки выполнения)
     * @throws RuntimeException if the thread is interrupted during sleep
     *                          (выбрасывается исключение, если поток был прерван во время сна)
     */
    private static void delay(int seconds)  {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /** <strong>Русский:</strong><br>
     * <code>Этот метод выводит детали электронного письма, включая ID письма, отправителя, тему и содержание сообщения.</code><br>
     * Если письмо содержит вложения, также будет выведена ссылка на вложения.<br>
     * Если домен отправителя совпадает с указанным доменом остановки, проверка писем прекратится с сообщением.<br>
     * </p>
     * <strong>English:</strong><br>
     * <code>This method prints the details of an email, including the email ID, sender, subject, and message content.</code><br>
     * If the email contains attachments, it will also print a link to the attachments.<br>
     * If the sender's domain matches the given stop domain, the email check will stop with a message.<br>
     * </p>
     *
     * @param emailItem the email details in a {@link JSONObject} <code>(детали письма в {@link JSONObject})</code>
     * @param stopDomain the domain after which email check will stop <code>(домен, после которого проверка писем прекратится)</code>
     */
    private static void printEmailDetails(JSONObject emailItem, String stopDomain,boolean debug) {
        print(GRAY, "Email ID", emailItem.getInt("mail_id") + "");
        // print(GRAY, "Timestamp", emailItem.getLong("mail_timestamp") + "");
        if (emailItem.has("att") && emailItem.getLong("att") > 0) {
            printAttachmentLink(emailItem, debug);
        }
        print(GRAY, "From", emailItem.getString("mail_from"));
        print(GRAY, "Subject", emailItem.getString("mail_subject"));
        print(GRAY, "Message", RESET + "\n" + BLUE + getEmailContent(emailItem.getInt("mail_id")) + RESET);

        if (emailItem.getString("mail_from").endsWith(stopDomain)) {
            print(RED, "Email from", stopDomain + " received. Stopping email check.");
        }
    }

    /**
     * <strong>Русский:</strong><br>
     * <code>Этот метод выполняет несколько попыток чтения электронной почты с указанного API-URL.</code>
     * Он проверяет наличие новых писем и выводит информацию о каждом письме. Если письмо приходит с указанного домена,
     * проверка завершится. Также есть возможность включить отладочную информацию для вывода.<br>
     * </p>
     * <strong>English:</strong><br>
     * <code>This method makes multiple attempts to check emails from the specified API URL.</code>
     * It checks for new emails and prints details of each email. If an email is received from the specified domain,
     * the check stops. There is also an option to enable debug output for more detailed response data.<br>
     * </p>
     *
     * @param apiUrl the URL of the API to check emails (URL API для проверки электронной почты)
     * @param numAttempts the number of attempts to check emails (количество попыток проверки электронной почты)
     * @param intervalAttempts the interval (in seconds) between attempts (интервал в секундах между попытками)
     * @param stopDomain the domain of the email sender after which the check will stop (домен отправителя, после которого проверка прекратится)
     * @param debug whether to print debug information (если true, выводится отладочная информация)
     */
    private static void checkEmails(String apiUrl, int numAttempts, int intervalAttempts, String stopDomain, boolean debug) {
        try {
            for (int attempt = 1; attempt <= numAttempts; attempt++) {
                print(YELLOW, "Attempt to check emails", attempt + "");
                JSONObject jsonResponse = makeApiCall(apiUrl);

                if (debug) {
                    printSelectedFieldsFromResponse(jsonResponse.toString(), apiUrl, setupConnection(apiUrl));
                    print(PURPLE, "Response", jsonResponse.toString(2));
                }

                if (jsonResponse.has("list")) {
                    JSONArray emailList = jsonResponse.getJSONArray("list");
                    print(GREEN, "New emails in box found", emailList.length() + "");
                    for (int i = 0; i < emailList.length(); i++) {
                        JSONObject emailItem = emailList.getJSONObject(i);
                        printEmailDetails(emailItem, stopDomain, debug);
                        if (emailItem.getString("mail_from").endsWith(stopDomain)) {
                            return; // Exit after receiving email from specified domain
                        }
                    }
                } else {
                    print(RED, "No emails found", "Attempt " + attempt + ": No new emails was found.");
                }

                if (attempt < numAttempts) {
                    delay(intervalAttempts);
                }
                System.out.println("----------------------------------------------------------------");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * <strong>Русский:</strong><br>
     * <code>Этот метод получает данные сеанса для указанного адреса электронной почты с целью получения sid_token.</code>
     * Он извлекает имя пользователя из адреса электронной почты, формирует URL для API и отправляет запрос для получения
     * данных сеанса, включая sid_token.<br>
     * </p>
     * <strong>English:</strong><br>
     * <code>This method retrieves session data for the specified email address in order to obtain the sid_token.</code>
     * It extracts the username from the email address, constructs the API URL, and sends a request to retrieve the session
     * data, including the sid_token.<br>
     * </p>
     *
     * @param emailAddress the email address for which session data is to be retrieved (адрес электронной почты, для которого нужно получить данные сеанса)
     * @return the <code>sid_token (sid_token)</code>
     */
    public static String getSessionData(String emailAddress) {
        String emailUser = emailAddress.split("@")[0];
        String apiUrl = API_URL + "?f=set_email_user&email_user=" + emailUser + "&lang=en";
        try {
            HttpURLConnection connection = setupConnection(apiUrl);
            String response = readResponse(connection);
            JSONObject jsonResponse = new JSONObject(response);
            setSidToken(jsonResponse.getString("sid_token"));
            print(PURPLE, "Session Data Received", CYAN + "Email: " + emailAddress + RESET);
            return jsonResponse.getString("sid_token");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * <strong>Русский:</strong><br>
     * <code>Этот метод выполняет запрос к API для получения случайного адреса электронной почты.</code>
     * Он возвращает полученный адрес или выводит ошибку, если запрос не удался.<br>
     * </p>
     * <strong>English:</strong><br>
     * <code>This method makes an API call to retrieve a random email address.</code>
     * It returns the obtained email address or prints an error if the request fails.<br>
     * </p>
     *
     * @return <code>a random email address (случайный адрес электронной почты)</code>
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
     * <strong>Русский:</strong><br>
     * <code>Этот метод выполняет запрос к API для получения содержимого электронного письма по его ID.</code>
     * Он извлекает и возвращает текстовое содержимое письма, удаляя HTML-теги. Если письмо не найдено или произошла ошибка,
     * выводится сообщение об ошибке.<br>
     * </p>
     * <strong>English:</strong><br>
     * <code>This method makes an API call to fetch the content of an email by its ID.</code>
     * It extracts and returns the text content of the email, removing HTML tags. If the email is not found or an error occurs,
     * an error message is printed.<br>
     * </p>
     *
     * @param mailId the ID of the email to fetch content for (ID электронного письма, для которого нужно получить содержимое)
     * @return <code>the email content as a text (содержимое электронного письма в текстовом виде)</code>
     */
    private static String getEmailContent(int mailId) {
        String apiUrl = API_URL + "?f=fetch_email&email_id=" + mailId + "&sid_token=" + sidToken;
        try {
            String response = getResponseFromUrl(apiUrl);
            JSONObject jsonResponse = new JSONObject(response);
            if (jsonResponse.has("mail_body")) {
                String mailBody = jsonResponse.getString("mail_body");
                // Extract text from HTML
                return prettyHTML(mailBody);
            } else {
                System.err.println("Failed to fetch email content: " + response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Failed to fetch email content.";
    }


    /**
     * <strong>Русский:</strong><br>
     * <code>Этот метод извлекает текст из HTML, удаляя все теги HTML и обрезая начальные и конечные пробелы.</code><br>
     * </p>
     * <strong>English:</strong><br>
     * <code>This method extracts text from HTML by removing all HTML tags and trimming leading and trailing whitespace.</code><br>
     * </p>
     *
     * @param html the HTML content from which to extract text (HTML-контент, из которого нужно извлечь текст)
     * @return the <code>extracted text without HTML tags (извлечённый текст без HTML тегов)</code>
     */
    private static String prettyHTML(String html) {
        return html.replaceAll("\\<.*?>", "").trim(); // Remove all HTML tags and trim leading and trailing whitespace
    }


    /**
     * <strong>Русский:</strong><br>
     * <code>Этот метод выполняет запрос по указанному URL и возвращает ответ в виде строки.</code><br>
     * </p>
     * <strong>English:</strong><br>
     * <code>This method makes a request to the specified URL and returns the response as a string.</code><br>
     * </p>
     *
     * @param apiUrl the URL to send the request to (URL, на который отправляется запрос)
     * @return the <code>response from the URL as a string (ответ от URL в виде строки)</code>
     * @throws Exception if an error occurs during the connection or reading the response (если возникает ошибка при подключении или чтении ответа)
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
     * <strong>Русский:</strong><br>
     * <code>Этот метод выводит выбранные поля из ответа API, включая HTTP заголовки и данные из JSON-ответа.</code><br>
     * Он отображает информацию о запросе и ответе, а также специфические поля из JSON-объекта, такие как alias, ts, auth, sid_token и другие.<br>
     * </p>
     * <strong>English:</strong><br>
     * <code>This method prints selected fields from the API response, including HTTP headers and data from the JSON response.</code><br>
     * It displays information about the request and response, as well as specific fields from the JSON object, such as alias, ts, auth, sid_token, and others.<br>
     * </p>
     *
     * @param jsonResponse the response from the API as a JSON string (ответ от API в виде строки JSON)
     * @param apiUrl the URL of the API that was called (URL API, к которому был сделан запрос)
     * @param connection the HTTP connection used for the API call (HTTP-соединение, использованное для запроса)
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
        print(PURPLE, "PHPSESSID", PHPSESSID);
    }


    /**
     * <strong>Русский:</strong><br>
     * <code>Этот метод настраивает базовое соединение с API Guerrilla Mail.</code><br>
     * Он устанавливает параметры соединения, такие как следование за редиректами, заголовки Accept и User-Agent, а также добавляет куки, если PHPSESSID доступен.<br>
     * </p>
     * <strong>English:</strong><br>
     * <code>This method sets up a basic connection to the Guerrilla Mail API.</code><br>
     * It configures connection properties such as following redirects, setting Accept and User-Agent headers, and adding the PHPSESSID cookie if available.<br>
     * </p>
     *
     * @param apiUrl the URL of the API to connect to (URL API для подключения)
     * @return the <code>established HttpURLConnection (установленное соединение HttpURLConnection)</code>
     * @throws RuntimeException if an error occurs while setting up the connection (выбрасывается исключение, если произошла ошибка при настройке соединения)
     */
    private static HttpURLConnection setupBasicConnection(String apiUrl) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            if (PHPSESSID != null) {
                connection.setRequestProperty("Cookie", "PHPSESSID=" + PHPSESSID);
            }
            return connection;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * <strong>Русский:</strong><br>
     * <code>Этот метод обрабатывает перенаправления и куки для соединения с API Guerrilla Mail.</code><br>
     * Он проверяет статус ответа на соединение и, если необходимо, выполняет перенаправление по новому URL. Также извлекает
     * куки из заголовков ответа, чтобы сохранить значение PHPSESSID для дальнейшего использования.<br>
     * </p>
     * <strong>English:</strong><br>
     * <code>This method handles redirects and cookies for the connection to the Guerrilla Mail API.</code><br>
     * It checks the response status and, if needed, follows the redirect to the new URL. It also extracts cookies from the
     * response headers to store the PHPSESSID value for future use.<br>
     * </p>
     *
     * @param connection the initial HTTP connection to the API (исходное соединение HTTP с API)
     * @return the <code>updated HTTP connection after handling redirects and cookies (обновленное соединение HTTP после обработки перенаправлений и куков)</code>
     * @throws Exception if an error occurs while handling redirects or extracting cookies (если произошла ошибка при обработке перенаправлений или извлечении куков)
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
     * <strong>Русский:</strong><br>
     * <code>Этот метод настраивает соединение с API Guerrilla Mail.</code><br>
     * Он вызывает метод для базовой настройки соединения, а затем обрабатывает перенаправления и куки, чтобы получить правильное соединение.<br>
     * </p>
     * <strong>English:</strong><br>
     * <code>This method sets up a connection to the Guerrilla Mail API.</code><br>
     * It calls the method for basic connection setup and then handles redirects and cookies to get the correct connection.<br>
     * </p>
     *
     * @param apiUrl the URL of the API to connect to (URL API для подключения)
     * @return the <code>configured HTTP connection (настроенное соединение HTTP)</code>
     * @throws Exception if an error occurs while setting up the connection (если произошла ошибка при настройке соединения)
     */
    private static HttpURLConnection setupConnection(String apiUrl) throws Exception {
        HttpURLConnection connection = setupBasicConnection(apiUrl);
        return handleRedirectsAndCookies(connection);
    }

    /**
     * <strong>Русский:</strong><br>
     * <code>Этот метод читает ответ от соединения HTTP.</code><br>
     * Он считывает данные из потока ввода соединения и возвращает их как строку.<br>
     * </p>
     * <strong>English:</strong><br>
     * <code>This method reads the response from an HTTP connection.</code><br>
     * It reads data from the input stream of the connection and returns it as a string.<br>
     * </p>
     *
     * @param connection the HTTP connection to read the response from (HTTP соединение для чтения ответа)
     * @return the <code>response as a string (ответ в виде строки)</code>
     * @throws Exception if an error occurs while reading the response (если произошла ошибка при чтении ответа)
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
     * <strong>Русский:</strong><br>
     * <code>Этот метод удаляет электронное письмо по его ID.</code><br>
     * Он получает данные сеанса для указанного адреса электронной почты, извлекает информацию о письме,
     * а затем удаляет его, если это возможно. После этого выводится информация о письме, если удаление прошло успешно.<br>
     * </p>
     * <strong>English:</strong><br>
     * <code>This method deletes an email by its ID.</code><br>
     * It retrieves session data for the given email address, fetches the email details,
     * and then deletes the email if possible. Afterward, it prints information about the email if the deletion was successful.<br>
     * </p>
     *
     * @param emailId the ID of the email to be deleted (ID письма для удаления)
     * @param emailToDelete the email address from which the session data is fetched (адрес электронной почты, с которого извлекаются данные сеанса)
     */
    public static void deleteEmail(int emailId, String emailToDelete) {
        String sidToken = getSessionData(emailToDelete);
        try {
            // Fetch email details before deletion
            String fetchEmailUrl = API_URL + "?f=fetch_email&email_id=" + emailId + "&sid_token=" + sidToken;
            HttpURLConnection fetchConnection = setupConnection(fetchEmailUrl);
            String fetchResponse = readResponse(fetchConnection);
            JSONObject fetchJsonResponse = new JSONObject(fetchResponse);

            // Proceed with deletion
            String apiUrl = API_URL + "?f=del_email&email_ids[]=" + emailId + "&sid_token=" + sidToken;
            HttpURLConnection connection = setupConnection(apiUrl);
            String response = readResponse(connection);
            JSONObject jsonResponse = new JSONObject(response);
            if (jsonResponse.has("deleted_ids")) {
                JSONArray deletedIds = jsonResponse.getJSONArray("deleted_ids");
                if (!deletedIds.isEmpty() && !"0".equals(deletedIds.getString(0))) {
                    // Print email details along with success message
                    String emailFrom = fetchJsonResponse.getString("mail_from");
                    String emailSubject = fetchJsonResponse.getString("mail_subject");
                    String emailDateStr = fetchJsonResponse.getString("mail_date");
                    String emailBody = prettyHTML(fetchJsonResponse.getString("mail_body"));

                    print(GREEN, "Email успешно удалён", "ID: " + (emailId == -1 ? "'-1' шаблонное письмо от сервиса" : emailId));
                    print(PURPLE, "От", emailFrom);
                    print(PURPLE, "Тема", emailSubject);
                    print(PURPLE, "Дата", emailDateStr.equals("00:00:00") ? "'00:00:00' Шаблонное письмо не имеет дату" : emailDateStr);
                    print(PURPLE, "Текст", emailBody);
                } else {
                    print(RED, "Не удалось удалить email или email ID не найден", response);
                }
            } else {
                print(RED, "Не удалось удалить email", response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * <strong>Русский:</strong><br>
     * <code>Этот метод удаляет все электронные письма на указанном адресе электронной почты.</code><br>
     * Сначала он получает данные сеанса для указанного адреса, затем извлекает список всех писем,
     * и удаляет каждое письмо, используя метод <code>deleteEmail</code>. Если писем нет, выводится соответствующее сообщение.<br>
     * </p>
     * <strong>English:</strong><br>
     * <code>This method deletes all emails on the specified email address.</code><br>
     * It first retrieves session data for the given email address, then fetches the list of all emails,
     * and deletes each email using the <code>deleteEmail</code> method. If there are no emails, it prints a message indicating so.<br>
     * </p>
     *
     * @param email the email address from which all emails are to be deleted <code>(адрес электронной почты, с которого нужно удалить все письма)</code>
     */
    public static void deleteAllEmails(String email) {
        String sidToken = getSessionData(email);
        try {
            // Fetch the list of all email IDs
            String fetchEmailListUrl = API_URL + "?f=get_email_list&offset=0&sid_token=" + sidToken;
            HttpURLConnection fetchConnection = setupConnection(fetchEmailListUrl);
            String fetchResponse = readResponse(fetchConnection);
            JSONObject fetchJsonResponse = new JSONObject(fetchResponse);

            if (fetchJsonResponse.has("list")) {
                JSONArray emailList = fetchJsonResponse.getJSONArray("list");

                if (emailList.length() == 0) {
                    print(GREEN, "No emails to delete", "The account " + GREEN + email + RESET + " has no emails.");
                    return;
                }

                for (int i = 0; i < emailList.length(); i++) {
                    JSONObject emailObj = emailList.getJSONObject(i);
                    int emailId = emailObj.getInt("mail_id");
                    // Use the deleteEmail method to delete each email
                    deleteEmail(emailId, sidToken);
                }
                print(GREEN, "All emails deleted", "All emails on the account have been successfully deleted.");
            } else {
                print(RED, "Failed to fetch email list", fetchResponse);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * <strong>Русский:</strong><br>
     * <code>Этот метод проверяет почту на указанном адресе с указанной задержкой и количеством попыток.</code><br>
     * Сначала осуществляется задержка перед проверкой, затем метод <code>checkEmails</code> проверяет почту с учетом параметров попыток и интервала.
     * После завершения выводится сообщение о завершении проверки. Если возникла ошибка, она будет напечатана в консоли.<br>
     * </p>
     * <strong>English:</strong><br>
     * <code>This method checks the email for the specified address with a given delay and number of attempts.</code><br>
     * First, a delay is applied before checking, then the <code>checkEmails</code> method checks the email according to the given number of attempts and interval.
     * After completion, a message is printed indicating the check completion. If an error occurs, it is printed to the console.<br>
     * </p>
     *
     * @param emailAddress the email address to check  <code>(адрес электронной почты для проверки)</code>
     * @param startDelay initial delay before starting the email check  <code>(задержка перед началом проверки)</code>
     * @param numAttempts number of attempts to check the email  <code>(количество попыток)</code>
     * @param intervalAttempts interval between attempts  <code>(интервал между попытками)</code>
     * @param stopDomain domain to stop checking at  <code>(домен для прекращения проверки)</code>
     * @param debug flag to enable debug messages  <code>(флаг для включения сообщений отладки)</code>
     */
    public static void readFromRandomEmail(String emailAddress, int startDelay, int numAttempts, int intervalAttempts, String stopDomain, boolean debug) {
        try {
            delay(startDelay); // Initial delay

            String apiUrl = API_URL + "?f=check_email&seq=0&email=" + emailAddress + "&sid_token=" + sidToken;
            checkEmails(apiUrl, numAttempts, intervalAttempts, stopDomain, debug);
            print(GREEN, "Email Check Complete", "All attempts to check emails completed.");
        } catch (Exception e) {
            print(RED, "Failed to check emails", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * <strong>Русский:</strong><br>
     * <code>Этот метод позволяет настроить пользователя и проверить его почту.</code><br>
     * Он регистрирует почтовый адрес, используя API Guerrilla Mail, получает токен сессии и сохраняет его. После этого выполняется задержка,
     * и почта проверяется с учетом заданных параметров, таких как количество попыток, интервал между попытками и домен, на котором проверка должна быть остановлена.<br>
     * </p>
     * <strong>English:</strong><br>
     * <code>This method sets up a user and checks their email.</code><br>
     * It registers the email address using the Guerrilla Mail API, retrieves the session token, and stores it. After a delay,
     * it checks the email according to the specified parameters such as number of attempts, interval between attempts, and the stop domain.<br>
     * </p>
     *
     * @param emailUser the email address of the user to be registered <code>(адрес для регистрации)</code>
     * @param startDelay initial delay before checking the email <code>(начальная задержка)</code>
     * @param numAttempts number of attempts to check the email <code>(количество попыток)</code>
     * @param intervalAttempts interval between attempts <code>(интервал между попытками)</code>
     * @param stopDomain domain to stop checking at <code>(домен для прекращения проверки)</code>
     * @param debug flag to enable debug messages <code>(флаг для включения сообщений отладки)</code>
     */
    public static void readFromIndividualEmail(String emailUser, int startDelay, int numAttempts, int intervalAttempts, String stopDomain, boolean debug) {
        try {
            String setEmailUserUrl = API_URL + "?f=set_email_user&email_user=" + emailUser.split("@")[0] + "&lang=en";
            JSONObject jsonResponse = makeApiCall(setEmailUserUrl);

            //String emailAddress = jsonResponse.getString("email_addr");
            String sidToken = jsonResponse.getString("sid_token");
            setSidToken(sidToken);
            print(PURPLE, "Registered Email Address", emailUser);

            if (debug) {
                printSelectedFieldsFromResponse(jsonResponse.toString(), setEmailUserUrl, setupConnection(setEmailUserUrl));
                print(PURPLE, "Response", jsonResponse.toString(2));
            }

            delay(startDelay);

            String checkEmailUrl = API_URL + "?f=check_email&seq=0&sid_token=" + sidToken;
            checkEmails(checkEmailUrl, numAttempts, intervalAttempts, stopDomain, debug);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Метод для печати ссылки на вложение в электронном письме:
    private static void printAttachmentLink(JSONObject email,boolean debug) {
        int emailId = email.getInt("mail_id");
        String apiUrl = API_URL + "?f=fetch_email&email_id=" + emailId + "&sid_token=" + getSidToken();
        try {
            String response = getResponseFromUrl(apiUrl);
            JSONObject jsonResponse = new JSONObject(response);
            if (debug) {
                // TODO: API response logging
                print(PURPLE, "API Response for email " + emailId, jsonResponse.toString(2));
                //System.out.println("API Response for email " + emailId + ": " + jsonResponse.toString(2));
            }
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
                        System.out.println(GRAY + "Download Link " + RESET + "[" + (i + 1) + "]: " + attachmentUrl + GRAY + ", File name:" + RESET + "[" + fileName + "]" + RESET + GRAY + ", File type: [" + RESET + fileType + "]");
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

    // Метод для выполнения GET-запроса к API Guerrilla Mail с целью получения идентификатора электронного письма:
    private static String performGetRequest(String apiUrl) {
        try {
            HttpURLConnection connection = setupConnection(apiUrl);
            return readResponse(connection);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Метод для получения идентификатора электронного письма по адресу электронной почты:
    public static int getEmailId(String emailAddress) {
        String sidToken = getSessionData(emailAddress);
        String apiUrl = API_URL + "?f=check_email&sid_token=" + sidToken + "&seq=20";
        String response = performGetRequest(apiUrl);
        if (response != null) {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                if (jsonResponse.has("list")) {
                    JSONArray emailList = jsonResponse.getJSONArray("list");
                    if (!emailList.isEmpty()) {
                        return emailList.getJSONObject(0).getInt("mail_id");
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    @Test
    public void createRandomAccount() {
        String randomEmailAddress = GuerrillaMailAPI.getRandomEmailAddress();
        readFromRandomEmail(randomEmailAddress, 1, 2, 10, "stop@email.com", false);
    }

    @Test
    public void createNewAccount() {
        String specificEmailAddress = "portishea9@guerrillamailblock.com";
        readFromIndividualEmail(specificEmailAddress, 1, 2, 10, "stop@email.com", false);
    }

    @Test
    public void deleteOneEMail() {
        String emailToDelete = "portishead9@guerrillamailblock.com";
        deleteEmail(getEmailId(emailToDelete), emailToDelete);
    }

    @Test
    public void deleteAllEmailsOnAccount() {
        deleteAllEmails("portishead9@guerrillamailblock.com");
    }
}
