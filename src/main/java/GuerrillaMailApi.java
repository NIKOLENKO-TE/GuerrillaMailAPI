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
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

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
public class GuerrillaMailApi {
    private static final Logger logger = Logger.getLogger(GuerrillaMailApi.class.getName());
    private static final String API_URL = "https://api.guerrillamail.com/ajax.php";
    private static String PHPSESSID = null; // Needed to store the PHP session ID to access the API
    private static String sidToken = null; // Needed to store the session token to access the API
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
     * @param key   the key to print (ключ для вывода)
     * @param value the value to print (значение для вывода)
     */
    private static void print(String color, String key, String value) {
        System.out.println(color + key + ": " + RESET + "[" + value + "]");
    }

    private static void print(String color, String key) {
        System.out.println(color + key + RESET);
    }

    public static String getSidToken() {
        return sidToken;
    }

    public static void setSidToken(String sidToken) {
        GuerrillaMailApi.sidToken = sidToken;
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
     *
     * @param seconds the number of seconds to delay the execution
     *                (количество секунд для задержки выполнения)
     * @throws RuntimeException if the thread is interrupted during sleep
     *                          (выбрасывается исключение, если поток был прерван во время сна)
     */
    private static void delay(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * <strong>Русский:</strong><br>
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
     * @param emailItem  the email details in a {@link JSONObject} <code>(детали письма в {@link JSONObject})</code>
     * @param stopDomain the domain after which email check will stop <code>(домен, после которого проверка писем прекратится)</code>
     */
    private static void printEmailDetails(JSONObject emailItem, String stopDomain, boolean debug, int emailIndex) {
        String mailDate = emailItem.getString("mail_date");
        if (!mailDate.contains("-")) {
            // Combine mail_date with mail_timestamp to form the full date and time
            long timestamp = emailItem.getLong("mail_timestamp");
            mailDate = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(timestamp * 1000));
        }
        print(GREEN, "****************** EMAIL № " + RESET + "[" + (emailIndex + 1) + "]" + GREEN + " ******************");
        print(GRAY, "EMAIL ID", emailItem.getInt("mail_id") + "");
        if (emailItem.has("att") && emailItem.getLong("att") > 0) {
            printAttachmentLink(emailItem, debug);
            if (emailItem.has("mail_size")) {
                print(GRAY, "SIZE", emailItem.getString("mail_size") + " bytes");
            } else {
                print(GRAY, "SIZE", "N/A");
            }
        }
        print(GRAY, "TIME", mailDate);
        print(GRAY, "FROM", emailItem.getString("mail_from"));
        print(GRAY, "SUBJECT", emailItem.getString("mail_subject"));
        print(GRAY, "MESSAGE", RESET + "\n" + BLUE + getEmailContent(emailItem.getInt("mail_id")) + RESET + "\n");

        if (emailItem.getString("mail_from").endsWith(stopDomain)) {
            print(RED, "Email from ", stopDomain + " received. Stopping email check.");
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
     * @param apiUrl           the URL of the API to check emails (URL API для проверки электронной почты)
     * @param numAttempts      the number of attempts to check emails (количество попыток проверки электронной почты)
     * @param intervalAttempts the interval (in seconds) between attempts (интервал в секундах между попытками)
     * @param stopDomain       the domain of the email sender after which the check will stop (домен отправителя, после которого проверка прекратится)
     * @param debug            whether to print debug information (если true, выводится отладочная информация)
     */
    private static void checkEmails(String apiUrl, int numAttempts, int intervalAttempts, String stopDomain, boolean debug) {
        try {
            for (int attempt = 1; attempt <= numAttempts; attempt++) {
                print(GRAY, String.valueOf(attempt), YELLOW + "Attempt is starting" + RESET);
                JSONObject jsonResponse = makeApiCall(apiUrl);

                if (debug) {
                    printSelectedFieldsFromResponse(jsonResponse.toString(), apiUrl, setupConnection(apiUrl));
                    print(getCallingMethodName() + PURPLE, ": RAW Response", jsonResponse.toString(2));
                }

                if (jsonResponse.has("list")) {
                    JSONArray emailList = jsonResponse.getJSONArray("list");
                    print(GRAY, attempt + ": " + RESET + "[" + YELLOW + "New emails in box found", emailList.length() + "");
                    for (int i = 0; i < emailList.length(); i++) {
                        JSONObject emailItem = emailList.getJSONObject(i);
                        printEmailDetails(emailItem, stopDomain, debug, i);
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
                print(GRAY, String.valueOf(attempt), YELLOW + "Attempt completed" + RESET);
                System.out.println("--------------------------------------------------");
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
            print(getCallingMethodName() + PURPLE, ": Session Data Received for Email", CYAN + emailAddress + RESET);
            return jsonResponse.getString("sid_token");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to get session data", e);
        }
        return null;
    }

    /**
     * <strong>Русский:</strong><br>
     * <code>Этот метод возвращает имя метода, который его вызвал</code><br>
     * </p>
     * <strong>English:</strong><br>
     * <code>This method returns the name of the method that called it</code><br>
     * </p>
     *
     * @return <code>the name of the calling method (имя вызывающего его метода)</code>
     */
    private static String getCallingMethodName() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        if (stackTraceElements.length >= 4) {
            return stackTraceElements[3].getMethodName();
        }
        return "Unknown";
    }

    /**
     * <strong>Русский:</strong><br>
     * <code>Метод для получения запроса в формате JSON.</code><br>
     * Этот метод принимает URL API, заголовки и метод запроса, и возвращает их в виде JSON-объекта.<br>
     * </p>
     * <strong>English:</strong><br>
     * <code>Method to get the request in JSON format.</code><br>
     * This method takes the API URL, headers, and request method, and returns them as a JSON object.<br>
     * </p>
     *
     * @param apiUrl        the URL of the API request (URL запроса API)
     * @param headers       the headers of the API request (заголовки запроса API)
     * @param requestMethod the HTTP method of the API request (HTTP метод запроса API)
     * @return the JSON representation of the request <code>(JSON представление запроса)</code>
     */
    public static String getRequestAsJson(String apiUrl, Map<String, List<String>> headers, String requestMethod) {
        JSONObject json = new JSONObject();
        String[] urlParts = apiUrl.split("\\?");
        json.put("base_url", urlParts[0]);
        if (urlParts.length > 1) {
            String[] params = urlParts[1].split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                json.put(keyValue[0], keyValue.length > 1 ? keyValue[1] : "");
            }
        }
        JSONObject headersJson = new JSONObject();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            headersJson.put(entry.getKey(), entry.getValue());
        }
        json.put("headers", headersJson);
        json.put("method", requestMethod);
        // printAllKeysAndValues(json);
        return json.toString(2);
    }

    /**
     * <strong>Русский:</strong><br>
     * <code>Этот метод добавляет детали ответа HTTP в JSON-объект и выводит их в консоль.</code><br>
     * Он добавляет код ответа, сообщение и тип содержимого в JSON-объект, а затем выводит его в консоль.<br>
     * </p>
     * <strong>English:</strong><br>
     * <code>This method adds HTTP response details to a JSON object and prints them to the console.</code><br>
     * It adds the response code, message, and content type to the JSON object, and then prints it to the console.<br>
     * </p>
     *
     * @param connection   the HTTP connection from which to retrieve response details (HTTP-соединение, из которого извлекаются детали ответа)
     * @param jsonResponse the JSON object to which response details are added (JSON-объект, в который добавляются детали ответа)
     * @throws IOException if an I/O error occurs while retrieving response details (если возникает ошибка ввода-вывода при получении деталей ответа)
     */
    public static void printResponseDetails(HttpURLConnection connection, JSONObject jsonResponse) throws IOException {
        jsonResponse.put("code", connection.getResponseCode());
        jsonResponse.put("message", connection.getResponseMessage());
        jsonResponse.put("content-type", connection.getContentType());
        print(getCallingMethodName() + PURPLE, ": RAW Response", jsonResponse.toString(2));
    }

    /**
     * <strong>Русский:</strong><br>
     * <code>Этот метод выводит ссылку на вложения в электронном письме.</code><br>
     * Он извлекает информацию о вложениях из JSON-объекта письма и выводит ссылку на каждое вложение.<br>
     * </p>
     * <strong>English:</strong><br>
     * <code>This method prints a link to the attachments in an email.</code><br>
     * It extracts information about the attachments from the JSON object of the email and prints a link to each attachment.<br>
     * </p>
     *
     * @param jsonObject the email details in a {@link JSONObject} <code>(детали письма в {@link JSONObject})</code>
     */
    // Temporarily not used
    public static void printAllKeysAndValues(JSONObject jsonObject) {
        for (String key : jsonObject.keySet()) {
            System.out.println(key + ": " + jsonObject.get(key));
        }
    }

    /**
     * <strong>Русский:</strong><br>
     * <code>Этот метод генерирует случайный адрес электронной почты, используя Guerrilla Mail API, и возвращает его.</code><br>
     * Он создает случайное имя пользователя, формирует полный адрес электронной почты, получает sid_token и отправляет запрос к API для получения адреса электронной почты.<br>
     * </p>
     * <strong>English:</strong><br>
     * <code>This method generates a random email address using the Guerrilla Mail API and returns it.</code><br>
     * It creates a random username, forms the full email address, retrieves the sid_token, and sends a request to the API to get the email address.<br>
     * </p>
     *
     * @param debug flag to enable debug messages <code>(флаг для включения сообщений отладки)</code>
     * @return the generated random email address (сгенерированный случайный адрес электронной почты)
     */
    public static String getRandomEmailAddress(boolean debug) {
        String randomEmailUser = UUID.randomUUID().toString().substring(0, 8);
        String emailAddress = randomEmailUser + "@guerrillamailblock.com";
        sidToken = getSessionData(emailAddress);
        String apiUrl = API_URL + "?f=get_email_address&lang=en&sid_token=" + sidToken;
        try {
            HttpURLConnection connection = setupBasicConnection(apiUrl);
            if (debug) {
                print(getCallingMethodName() + PURPLE, ": RAW Request", getRequestAsJson(apiUrl, connection.getRequestProperties(), connection.getRequestMethod()));
            }
            connection = setupConnection(apiUrl);
            String response = readResponse(connection);
            JSONObject jsonResponse = new JSONObject(response);
            if (debug) {
                printResponseDetails(connection, jsonResponse);
                // printAllKeysAndValues(jsonResponse); // Print all keys and values
            }
            setSidToken(jsonResponse.getString("sid_token"));
            if (jsonResponse.has("email_addr")) {
                String emailAddr = jsonResponse.getString("email_addr");
                print(getCallingMethodName() + PURPLE, ": Random Email Address", CYAN + emailAddr + RESET);
                return emailAddr;
            } else {
                logger.log(Level.SEVERE, "Failed to get random email address: " + response);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to get random email address", e);
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
     * @return the email content as a text <code>(содержимое электронного письма в текстовом виде)</code>
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
            logger.log(Level.SEVERE, "Failed to fetch email content", e);
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
     * @return the extracted text without HTML tags <code>(извлечённый текст без HTML тегов)</code>
     */
    private static String prettyHTML(String html) {
        return html.replaceAll("<.*?>", "").trim(); // Remove all HTML tags and trim leading and trailing whitespace
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
     * @return the response from the URL as a string <code>(ответ от URL в виде строки)</code>
     * @throws Exception if an error occurs during the connection or reading the response (если возникает ошибка при подключении или чтении ответа)
     */
    private static String getResponseFromUrl(String apiUrl) throws Exception {
        HttpURLConnection connection = setupConnection(apiUrl);
        return getString(connection);
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
     * @param apiUrl       the URL of the API that was called (URL API, к которому был сделан запрос)
     * @param connection   the HTTP connection used for the API call (HTTP-соединение, использованное для запроса)
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
            logger.log(Level.SEVERE, "Failed to print selected fields from response", e);
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
     * @return the updated HTTP connection after handling redirects and cookies <code>(обновленное соединение HTTP после обработки перенаправлений и куков)</code>
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
     * @return the configured HTTP connection <code>(настроенное соединение HTTP)</code>
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
     * @return the response as a string <code>(ответ в виде строки)</code>
     * @throws Exception if an error occurs while reading the response (если произошла ошибка при чтении ответа)
     */
    private static String readResponse(HttpURLConnection connection) throws Exception {
        return getString(connection);
    }

    private static String getString(HttpURLConnection connection) throws IOException {
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
     * @param emailId       the ID of the email to be deleted (ID письма для удаления)
     * @param debug         flag to enable debug messages (флаг для включения сообщений отладки)
     * @param emailToDelete the email address from which the session data is fetched (адрес электронной почты, с которого извлекаются данные сеанса)
     */
    public static void deleteEmail(int emailId, String emailToDelete, boolean debug) {
        String sidToken = getSessionData(emailToDelete);
        if (debug) {
            try {
                // Print debug information even if no emails are found
                String fetchEmailUrl = API_URL + "?f=fetch_email&email_id=" + emailId + "&sid_token=" + sidToken;
                HttpURLConnection fetchConnection = setupBasicConnection(fetchEmailUrl);
                print(getCallingMethodName() + PURPLE, ": RAW Request", getRequestAsJson(fetchEmailUrl, fetchConnection.getRequestProperties(), fetchConnection.getRequestMethod()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if (emailId == -1) {
            print(RED, "No email to delete", "No emails found for the address: " + emailToDelete);
            return;
        }
        try {
            // Get email details before deleting
            String fetchEmailUrl = API_URL + "?f=fetch_email&email_id=" + emailId + "&sid_token=" + sidToken;
            HttpURLConnection fetchConnection = setupBasicConnection(fetchEmailUrl);
            String fetchResponse = readResponse(fetchConnection);
            JSONObject fetchJsonResponse = new JSONObject(fetchResponse);
            if (debug) {
                printResponseDetails(fetchConnection, fetchJsonResponse);
            }

            // Proceed to delete
            String apiUrl = API_URL + "?f=del_email&email_ids[]=" + emailId + "&sid_token=" + sidToken;
            HttpURLConnection connection = setupBasicConnection(apiUrl);
            if (debug) {
                print(getCallingMethodName() + PURPLE, ": RAW Request", getRequestAsJson(apiUrl, connection.getRequestProperties(), connection.getRequestMethod()));
            }
            String response = readResponse(connection);
            JSONObject jsonResponse = new JSONObject(response);
            if (debug) {
                printResponseDetails(connection, jsonResponse);
            }
            if (jsonResponse.has("deleted_ids")) {
                print(GREEN, "Deleted Email ID", emailId + "");
            } else {
                print(RED, "Failed to delete email", "Response: " + response);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to delete email", e);
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
     * @param debug flag to enable debug messages <code>(флаг для включения сообщений отладки)</code>
     */
    public static void deleteAllEmails(String email, boolean debug) {
        String sidToken = getSessionData(email);
        try {
            // Fetch the list of all email IDs
            String fetchEmailListUrl = API_URL + "?f=get_email_list&offset=0&sid_token=" + sidToken;
            HttpURLConnection fetchConnection = setupConnection(fetchEmailListUrl);
            String fetchResponse = readResponse(fetchConnection);
            JSONObject fetchJsonResponse = new JSONObject(fetchResponse);

            if (fetchJsonResponse.has("list")) {
                JSONArray emailList = fetchJsonResponse.getJSONArray("list");
                if (emailList.isEmpty()) {
                    print(GREEN, "No emails to delete", "The account '" + CYAN + email + RESET + "' has no emails.");
                    return;
                }
                for (int i = 0; i < emailList.length(); i++) {
                    JSONObject emailObj = emailList.getJSONObject(i);
                    int emailId = emailObj.getInt("mail_id");
                    // Delete each email
                    deleteEmail(emailId, email, debug);
                    print(PURPLE, "Email ID", emailId + PURPLE + " was successfully deleted" + RESET);
                    System.out.println("********************************************");
                }
                print(GREEN, "All emails deleted", "All emails on the account '" + CYAN + email + RESET + "' have been successfully deleted.");
            } else {
                print(RED, "Failed to fetch email list", fetchResponse);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to delete all emails", e);
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
     * @param emailAddress     the email address to check  <code>(адрес электронной почты для проверки)</code>
     * @param startDelay       initial delay before starting the email check  <code>(задержка перед началом проверки)</code>
     * @param numAttempts      number of attempts to check the email  <code>(количество попыток)</code>
     * @param intervalAttempts interval between attempts  <code>(интервал между попытками)</code>
     * @param stopDomain       domain to stop checking at  <code>(домен для прекращения проверки)</code>
     * @param debug            flag to enable debug messages  <code>(флаг для включения сообщений отладки)</code>
     */
    public static void readFromRandomEmail(String emailAddress, int startDelay, int numAttempts, int intervalAttempts, String stopDomain, boolean debug) {
        try {
            delay(startDelay); // Initial delay
            String apiUrl = API_URL + "?f=check_email&seq=0&email=" + emailAddress + "&sid_token=" + sidToken;
            checkEmails(apiUrl, numAttempts, intervalAttempts, stopDomain, debug);
            print(GREEN, "Email Check Complete", "All attempts to check emails completed.");
        } catch (Exception e) {
            print(RED, "Failed to check emails", e.getMessage());
            logger.log(Level.SEVERE, "Failed to check emails", e);
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
     * @param emailUser        the email address of the user to be registered <code>(адрес для регистрации)</code>
     * @param startDelay       initial delay before checking the email <code>(начальная задержка)</code>
     * @param numAttempts      number of attempts to check the email <code>(количество попыток)</code>
     * @param intervalAttempts interval between attempts <code>(интервал между попытками)</code>
     * @param stopDomain       domain to stop checking at <code>(домен для прекращения проверки)</code>
     * @param debug            flag to enable debug messages <code>(флаг для включения сообщений отладки)</code>
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
                print(getCallingMethodName() + PURPLE, ": RAW Response", jsonResponse.toString(2));
            }

            delay(startDelay);

            String checkEmailUrl = API_URL + "?f=check_email&seq=0&sid_token=" + sidToken;
            checkEmails(checkEmailUrl, numAttempts, intervalAttempts, stopDomain, debug);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to read from individual email", e);
        }
    }

    /**
     * <strong>Русский:</strong><br>
     * <code>Метод для печати ссылки на вложение в электронном письме.</code><br>
     * Этот метод использует ID письма для запроса его содержимого через Guerrilla Mail API. Если письмо содержит вложения,
     * метод выводит ссылки для скачивания, имя файла и тип файла. Включает сообщения отладки, если параметр debug установлен в true.<br>
     * </p>
     * <strong>English:</strong><br>
     * <code>Method for printing the attachment link in an email.</code><br>
     * This method uses the email ID to fetch the content through the Guerrilla Mail API. If the email has attachments,
     * it prints the download links, file name, and file type. Debug messages are shown if the debug parameter is set to true.<br>
     * </p>
     *
     * @param email the email object containing email details <code>(объект письма с деталями письма)</code>
     * @param debug flag to enable debug messages <code>(флаг для включения сообщений отладки)</code>
     */
    private static void printAttachmentLink(JSONObject email, boolean debug) {
        int emailId = email.getInt("mail_id");
        String apiUrl = API_URL + "?f=fetch_email&email_id=" + emailId + "&sid_token=" + getSidToken();
        try {
            String response = getResponseFromUrl(apiUrl);
            JSONObject jsonResponse = new JSONObject(response);
            if (debug) {
                print(getCallingMethodName() + PURPLE, ": Attachment Response for email " + emailId, jsonResponse.toString(2));
            }
            if (jsonResponse.has("mail_body")) {
                if (jsonResponse.has("att") && jsonResponse.getInt("att") > 0) {
                    print(GRAY, "Attachments", jsonResponse.getInt("att") + "");
                    JSONArray attInfoArray = jsonResponse.getJSONArray("att_info");
                    for (int i = 0; i < attInfoArray.length(); i++) {
                        JSONObject attInfo = attInfoArray.getJSONObject(i);
                        String fileType = attInfo.getString("t");
                        String fileName = attInfo.getString("f");
                        String partId = attInfo.getString("p");
                        String attachmentUrl = "https://www.guerrillamail.com/inbox?get_att&email_id=" + emailId + "&part_id=" + partId + "&sid_token=" + getSidToken();
                        print(GRAY, "    Attachment", String.valueOf((i + 1)));
                        System.out.println(GRAY + "    " + "    File Link: " + RESET + attachmentUrl + GRAY + "\n    " + "    File Name: " + RESET + fileName + RESET + GRAY + "\n    " + "    File Type: " + RESET + fileType);
                    }
                } else {
                    print(GRAY, "Attachments", "No attachments found");
                }
            } else {
                print(RED, "Failed to fetch attachment link", response);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to print attachment link", e);
        }
    }

    /**
     * <strong>Русский:</strong><br>
     * <code>Метод для выполнения GET-запроса к API Guerrilla Mail с целью получения идентификатора электронного письма.</code><br>
     * Этот метод устанавливает соединение с указанным URL API, отправляет GET-запрос
     * и считывает ответ от сервера. В случае ошибки печатает трассировку стека и возвращает null.<br>
     * </p>
     * <strong>English:</strong><br>
     * <code>Method for performing a GET request to the Guerrilla Mail API to fetch the email ID.</code><br>
     * This method establishes a connection to the specified API URL, sends a GET request,
     * and reads the response from the server. In case of an error, it prints the stack trace and returns null.<br>
     * </p>
     *
     * @param apiUrl the URL to which the GET request is sent <code>(URL, по которому отправляется GET-запрос)</code>
     * @return the response from the server as a String, or null if an exception occurs <code>(ответ от сервера в виде строки или null в случае ошибки)</code>
     */
    private static String performGetRequest(String apiUrl) {
        try {
            HttpURLConnection connection = setupConnection(apiUrl);
            return readResponse(connection);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to perform GET request", e);
            return null;
        }
    }

    /**
     * <strong>Русский:</strong><br>
     * <code>Метод для получения ID электронного письма по адресу электронной почты.</code><br>
     * Этот метод использует указанный адрес электронной почты для получения sidToken, после чего отправляет запрос к API Guerrilla Mail,
     * чтобы получить список электронных писем. Если список не пустой, возвращается идентификатор первого письма.
     * В случае ошибки или пустого ответа возвращает -1.
     *
     * <br>
     *
     * <strong>English:</strong><br>
     * <code>Method to fetch the email ID using an email address.</code><br>
     * This method uses the provided email address to get the sidToken, then sends a request to the Guerrilla Mail API
     * to retrieve the list of emails. If the list is not empty, it returns the ID of the first email.
     * Returns -1 in case of an error or an empty response.
     *
     * @param emailAddress <code>адрес электронной почты для получения ID</code> (the email address to fetch the ID for)
     * @param debug        <code>флаг для включения отладочных сообщений</code> (flag to enable debug messages)
     * @return <code>ID первого электронного письма или -1 в случае ошибки</code> (the ID of the first email, or -1 if an error occurs)
     */
    private static int getEmailId(String emailAddress, boolean debug) {
        String sidToken = getSessionData(emailAddress);
        String apiUrl = API_URL + "?f=check_email&sid_token=" + sidToken + "&seq=20";
        if (debug) {
            try {
                HttpURLConnection connection = setupBasicConnection(apiUrl);
                print(getCallingMethodName() + PURPLE, ": RAW Request", getRequestAsJson(apiUrl, connection.getRequestProperties(), connection.getRequestMethod()));
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to print raw request", e);
            }
        }
        String response = performGetRequest(apiUrl);
        if (response != null) {
            if (debug) {
                print(getCallingMethodName() + PURPLE, ": RAW Response", response);
            }
            try {
                JSONObject jsonResponse = new JSONObject(response);
                if (jsonResponse.has("list")) {
                    JSONArray emailList = jsonResponse.getJSONArray("list");
                    if (!emailList.isEmpty()) {
                        return emailList.getJSONObject(0).getInt("mail_id");
                    }
                }
            } catch (JSONException e) {
                logger.log(Level.SEVERE, "Failed to get email ID", e);
            }
        }
        return -1; // Return -1 if no emails are found. -1 this is ID of template email
    }

    /**
     * <strong>Русский:</strong><br>
     * <code>Тест для создания случайного почтового аккаунта с использованием Guerrilla Mail и проверки входящих писем.</code><br>
     * Этот метод генерирует случайный адрес электронной почты через API Guerrilla Mail и инициирует процесс проверки
     * входящих писем на этот адрес. Выполняется указанное количество попыток с заданным интервалом, а также проверка,
     * есть ли письма с домена, при котором проверка должна быть остановлена. Можно включить или отключить режим отладки.
     * <p>
     * <strong>English:</strong><br>
     * <code>Test method to create a random email account using Guerrilla Mail and check for incoming emails.</code><br>
     * This method generates a random email address via the Guerrilla Mail API and initiates the process of checking
     * for incoming emails on that address. It performs the specified number of attempts with a given interval
     * and checks if any email matches a specified stop domain. Debugging mode can be toggled on or off.
     */
    @Test
    public void readFromRandomEmailAccount() {
        String randomEmailAddress = getRandomEmailAddress(false);
        readFromRandomEmail(randomEmailAddress, 1, 2, 10, "stop@domain.com", false);
    }

    /**
     * <strong>Русский:</strong><br>
     * <code>Тест создания нового почтового аккаунта с использованием конкретного адреса электронной почты, который вы можете задать самостоятельно.</code><br>
     * Этот метод использует указанный адрес электронной почты и вызывает метод для проверки входящих писем. Выполняется указанное
     * количество попыток с заданным интервалом между ними. Также проверяется, есть ли письма с домена, при котором проверка
     * должна быть остановлена. Возможность включения режима отладки.
     * <p>
     * <strong>English:</strong><br>
     * <code>Test method to create a new email account using a specific email address.</code><br>
     * This method uses the given email address and calls the method to check incoming emails. It performs the specified
     * number of attempts with a set interval between them. It also checks for emails from a stop domain to halt the check.
     * Debugging mode can be enabled or disabled.
     */
    @Test
    public void readFromIndividualEmailAccount() {
        String specificEmailAddress = "portishea10@guerrillamailblock.com";
        readFromIndividualEmail(specificEmailAddress, 1, 2, 10, "stop@domain.ua", false);
    }

    /**
     * <strong>Русский:</strong><br>
     * <code>Тест удаления первого электронного письма по заданному адресу.</code><br>
     * Этот метод использует указанный адрес электронной почты для получения его идентификатора и затем вызывает метод для
     * удаления письма с полученным идентификатором.
     * <p>
     * <strong>English:</strong><br>
     * <code>Test  to delete first email using a specified email address.</code><br>
     * This method uses the provided email address to fetch its ID and then calls the method to delete the email with the
     * fetched ID.
     */
    @Test
    public void deleteOneEMail() {
        String emailToDelete = "portishea10@guerrillamailblock.com";
        deleteEmail(getEmailId(emailToDelete, false), emailToDelete, false);
    }

    /**
     * <strong>Русский:</strong><br>
     * <code>Тест удаления всех электронных писем на указанном адресе электронной почты.</code><br>
     * Этот метод вызывает функцию удаления всех писем на аккаунте, используя заданный адрес электронной почты.
     * <p>
     * <strong>English:</strong><br>
     * <code>Test method to delete all emails on the specified email address.</code><br>
     * This method calls the function to delete all emails on the account using the provided email address.
     */
    @Test
    public void deleteAllEmailsOnAccount() {
        deleteAllEmails("portishea10@guerrillamailblock.com", false);
    }
}
