# 📧 GuerrillaMailAPI 📬

A Java utility to interact with the Guerrilla Mail API for creating and managing temporary email addresses. This project demonstrates how to retrieve new email addresses, check the inbox for new emails, and fetch email content using Guerrilla Mail's API.

## 📋 Table of Contents

- [Features](#features)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Usage](#usage)
- [Examples](#examples)
- [Development](#development)
- [Tests](#tests)
- [Contributing](#contributing)

## ✨ Features

- **Retrieve a new email address**: Get a fresh temporary email address from Guerrilla Mail.
- **Check inbox for new emails**: Check the inbox for new emails with configurable retry attempts and intervals.
- **Fetch email content**: Retrieve the content of an email by its ID.

## 🔧 Prerequisites

- Java Development Kit (JDK) 8 or higher
- Internet connection (for API requests)

## 📦 Installation

1. **Clone the repository:**

    ```sh
    git clone https://github.com/NIKOLENKO-TE/read_random_email.git
    ```

2. **Navigate to the project directory:**

    ```sh
    cd GuerrillaMailAPI
    ```

3. **Build the project using your preferred build tool (e.g., Maven or Gradle).**

## 🚀 Usage

### 📜 Retrieve a New Email Address

To retrieve a new email address:

```java
public class Main {
    public static void main(String[] args) {
        String emailAddress = GuerrillaMailAPI.getEmailAddress();
        System.out.println("Generated Email: " + emailAddress);
    }
}
```

### 📋 Check Inbox for New Emails

To check the inbox for new emails:

```java
public class Main {
    public static void main(String[] args) {
        String emailAddress = GuerrillaMailAPI.getEmailAddress();
        GuerrillaMailAPI.checkInbox(emailAddress, 1, 50, 5, "stop@eemail.com");
    }
}
```

### 📋 Fetch Email Content

To fetch the content of an email by its ID:

```java
public class Main {
    public static void main(String[] args) {
        String emailAddress = GuerrillaMailAPI.getEmailAddress();
        int mailId = 123456; // Replace with actual mail ID
        String content = GuerrillaMailAPI.getEmailContent(mailId);
        System.out.println("Email Content: " + content);
    }
}
```

## 📝 Examples

#### Example 1: 🧪 Generate and Print an Email Address

Certainly! Here are the examples in English based on your test cases:

### 🧪 Example 1: Create and Retrieve a Random Email Account

```java
public class Main {
    public static void main(String[] args) {
        // Generate a random email address
        String randomEmailAddress = GuerrillaMailAPI.getEmailAddress();
        System.out.println("Generated Random Email: " + randomEmailAddress);
        
        // Check inbox for new emails using the generated address
        GuerrillaMailAPI.checkInbox(randomEmailAddress, 1, 5, 20, "stop@email.com");
    }
}
```

### 🧪 Example 2: Create and Retrieve a Specific Email Account

```java
public class Main {
    public static void main(String[] args) {
        // Use a predefined email address
        String specificEmailAddress = "portishead@guerrillamailblock.com";
        System.out.println("Using Specific Email: " + specificEmailAddress);
        
        // Check inbox for new emails using the specified address
        GuerrillaMailAPI.checkInbox(specificEmailAddress, 1, 5, 10, "stop@email.com");
    }
}
```

### 🧪 Example 3: Delete an Email Account

```java
public class Main {
    public static void main(String[] args) {
        // Specify the email address to delete
        String emailToDelete = "portishead5@guerrillamailblock.com";
        System.out.println("Email to Delete: " + emailToDelete);
        
        // Retrieve the session data (sidToken) for the specified email address
        String sidTokenEmailToDelete = GuerrillaMailAPI.getSessionData(emailToDelete);
        
        // Get the email ID and delete the specified email address
        int emailId = GuerrillaMailAPI.getEmailId(emailToDelete, sidTokenEmailToDelete);
        GuerrillaMailAPI.deleteEmail(emailId, sidTokenEmailToDelete);
        System.out.println("Deleted Email: " + emailToDelete);
    }
}
```

Each example demonstrates how to use the Guerrilla Mail API for different tasks, such as creating and retrieving random and specific email addresses, as well as deleting an email account.

## 🛠️ Development

#### ⚙️ Setup

* Ensure you have **Java** and **Gradle** (or **Maven**) installed.
* Clone the repository.
* Import the project into your preferred IDE.

## 🧪 Running Tests

The project includes a few tests to demonstrate the usage of the API. You can run the tests using Gradle or Maven:

For Gradle:

```sh
./gradlew test
```
or
```sh
@Test
```

## 🧪 Tests

### Test 1: Create and Retrieve a Random Email Account

```java
@Test
public void createRandomAccount() {
    String randomEmailAddress = GuerrillaMailAPI.getEmailAddress();
    System.out.println("Generated Random Email: " + randomEmailAddress);
    GuerrillaMailAPI.checkInbox(randomEmailAddress, 1, 5, 20, "stop@email.com");
}
```

### Test 2: Create and Retrieve a Specific Email Account

```java
@Test
public void createNewAccount() {
    String specificEmailAddress = "portishead@guerrillamailblock.com";
    System.out.println("Using Specific Email: " + specificEmailAddress);
    GuerrillaMailAPI.checkInbox(specificEmailAddress, 1, 5, 10, "stop@email.com");
}
```

### Test 3: Delete an Email Account

```java
@Test
public void deleteAccount() {
    String emailToDelete = "portishead5@guerrillamailblock.com";
    String sidTokenEmailToDelete = GuerrillaMailAPI.getSessionData(emailToDelete);
    int emailId = GuerrillaMailAPI.getEmailId(emailToDelete, sidTokenEmailToDelete);
    GuerrillaMailAPI.deleteEmail(emailId, sidTokenEmailToDelete);
    System.out.println("Deleted Email: " + emailToDelete);
}
```

## 💡 Contributing

Feel free to contribute to this project by submitting a pull request or opening an issue. Happy coding! 😊

---

## Guerrilla Mail JSON API Documentation


### Introduction

Guerrilla Mail provides a public JSON API accessible via HTTP. It does not require registration or API keys. The API URL is: [https://api.guerrillamail.com/ajax.php](https://api.guerrillamail.com/ajax.php)

Requests must include the `f` parameter to specify the function. Optional parameters include `ip` and `agent` for client identification. Responses are in JSON format.

### Authorization

For private sites with custom domains, an Authorization Token is required. Create an account and set up your domain at [https://grr.la/ryo/guerrillamail.com/](https://grr.la/ryo/guerrillamail.com/). Set the site to ‘private’ if needed.

- **API Key**: Found in the control panel under Setup -> API page.
- **Authorization Header**: Include in each API call:
  ```
  Authorization: ApiToken YOUR_API_TOKEN
  ```

### API Functions

#### 1. `get_email_address`

**Arguments:**
- `lang` (optional) - Language code (e.g., `en`, `fr`).
- `sid_token` (optional) - Session ID token.
- `site` (optional) - Site identifier for custom domains.

**Description:**
Initializes a session and provides an email address. Returns `email_addr`, `email_timestamp`, and `sid_token`.

**Example Request:**
```
https://api.guerrillamail.com/ajax.php?f=get_email_address&lang=en&site=guerrillamail.com
```

**Response:**
```json
{
   "email_addr":"test@guerrillamailblock.com",
   "email_timestamp":1405770047,
   "sid_token":"lmmb0hfqa6qjoduvr2vdenas62"
}
```

#### 2. `set_email_user`

**Arguments:**
- `email_user` - Local part of the email address.
- `lang` (optional) - Language code.
- `sid_token` - Session ID token.
- `site` (optional) - Site identifier for custom domains.

**Description:**
Sets a new email address or extends the current one.

**Example Request:**
```
https://api.guerrillamail.com/ajax.php?f=set_email_user&email_user=test&site=guerrillamail.com&lang=en&sid_token=lmmb0hfqa6qjoduvr2vdenas62
```

**Response:**
```json
{
   "email_addr":"test@guerrillamailblock.com",
   "email_timestamp":1405770047,
   "sid_token":"lmmb0hfqa6qjoduvr2vdenas62"
}
```

#### 3. `check_email`

**Arguments:**
- `sid_token` - Session ID token.
- `seq` - Sequence number of the oldest email.

**Description:**
Checks for new emails and returns a list of up to 20 items.

**Example Request:**
```
https://api.guerrillamail.com/ajax.php?f=check_email&seq=0&sid_token=lmmb0hfqa6qjoduvr2vdenas62
```

**Response:**
```json
{
   "list":[
      {
         "mail_id":"23518194",
         "mail_from":"info@brahminmatrimony.com",
         "mail_subject":"Ravi Shastri, You have 50 potential matches",
         "mail_excerpt":"You have not viewed these profiles in detail.",
         "mail_timestamp":"1423958802",
         "mail_read":"0",
         "mail_date":"00:06:42"
      }
   ],
   "count":"1",
   "email":"test@guerrillamailblock.com",
   "sid_token":"jogbasvjjes145uej10hv70v67"
}
```

#### 4. `get_email_list`

**Arguments:**
- `sid_token` - Session ID token.
- `offset` - Number of emails to skip (starts from 0).
- `seq` (optional) - Sequence number of the first email.

**Description:**
Gets a list of emails starting from the specified offset. Maximum of 20 emails can be retrieved.

**Example Request:**
```
https://api.guerrillamail.com/ajax.php?f=get_email_list&offset=0&sid_token=lmmb0hfqa6qjoduvr2vdenas62
```

**Response:**
```json
{
   "list":[
      {
         "mail_id":"23518264",
         "mail_from":"bounce1@worldlargestsafelist.com",
         "mail_subject":"MULTIPLY Your Traffic, Leads & Subscribers to INFINITY!",
         "mail_excerpt":"Aloha from beautiful Hawai`i ...",
         "mail_timestamp":"1423959105",
         "mail_read":"0",
         "mail_date":"00:11:45"
      }
   ],
   "count":"1",
   "email":"test@guerrillamailblock.com",
   "sid_token":"jogbasvjjes145uej10hv70v67"
}
```

#### 5. `fetch_email`

**Arguments:**
- `sid_token` - Session ID token.
- `email_id` - ID of the email to fetch.

**Description:**
Fetches the content of an email, filtering HTML and images as described.

**Example Request:**
```
https://api.guerrillamail.com/ajax.php?f=fetch_email&email_id=23518264&sid_token=lmmb0hfqa6qjoduvr2vdenas62
```

**Response:**
```json
{
   "mail_id":"23518264",
   "mail_from":"bounce1@worldlargestsafelist.com",
   "mail_recipient":"test@guerrillamailblock.com",
   "mail_subject":"MULTIPLY Your Traffic, Leads & Subscribers to INFINITY!",
   "mail_excerpt":"Aloha from beautiful Hawai`i ...",
   "mail_body":"The message part of the email.",
   "mail_timestamp":"1423959105",
   "mail_date":"00:11:45",
   "mail_read":"0",
   "content_type":"text/html",
   "sid_token":"lmmb0hfqa6qjoduvr2vdenas62"
}
```

#### 6. `forget_me`

**Arguments:**
- `sid_token` - Session ID token.
- `email_addr` - Email address to forget.

**Description:**
Forgets the current email address but keeps the session active.

**Example Request:**
```
https://api.guerrillamail.com/ajax.php?f=forget_me&email_addr=test@guerrillamailblock.com&sid_token=lmmb0hfqa6qjoduvr2vdenas62
```

**Response:**
```json
true
```

#### 7. `del_email`

**Arguments:**
- `sid_token` - Session ID token.
- `email_ids` - Array of email IDs to delete.

**Description:**
Deletes specified emails from the server.

**Example Request:**
```
https://api.guerrillamail.com/ajax.php?f=del_email&sid_token=lmmb0hfqa6qjoduvr2vdenas62&email_ids[]=425&email_ids[]=426&email_ids[]=427
```

**Response:**
```json
{
   "deleted_ids":[425,426,427]
}
```

#### 8. `get_older_list`

**Arguments:**
- `sid_token` - Session ID token.
- `seq` - Sequence number for older emails.
- `limit` - Number of emails to fetch.

**Description:**
Fetches emails with IDs lower than the specified `seq`.

**Example Request:**
```
https://api.guerrillamail.com/ajax.php?f=get_older_list&seq=1000&limit=10&

sid_token=lmmb0hfqa6qjoduvr2vdenas62
```

**Response:**
```json
{
   "list":[
      {
         "mail_id":"23456789",
         "mail_from":"test@example.com",
         "mail_subject":"Older Email",
         "mail_excerpt":"This is an older email.",
         "mail_timestamp":"1423958500",
         "mail_read":"1",
         "mail_date":"00:00:00"
      }
   ],
   "count":"1",
   "email":"test@guerrillamailblock.com",
   "sid_token":"lmmb0hfqa6qjoduvr2vdenas62"
}
```

### Error Handling

API responses include an error field in case of invalid requests. Common errors include:

- `error_code` - Code indicating the type of error.
- `error_message` - Description of the error.

**Example Error Response:**
```json
{
   "error_code": 400,
   "error_message": "Invalid parameter 'site'."
}
```

### Notes
- Ensure correct usage of tokens and handle expired sessions.
- API rate limits may apply based on usage patterns.

For more detailed usage, refer to the API's interactive documentation at [https://api.guerrillamail.com](https://api.guerrillamail.com).

---