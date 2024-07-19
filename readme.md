# 📧 GuerrillaMailAPI

A Java utility to interact with the Guerrilla Mail API for creating and managing temporary email addresses. This project demonstrates how to retrieve new email addresses, check inbox for new emails, and fetch email content using Guerrilla Mail's API.

## 📋 Table of Contents

- [Features](https://github.com/NIKOLENKO-TE/read_random_email/?tab=readme-ov-file#-features)
- [Prerequisites](https://github.com/NIKOLENKO-TE/read_random_email/?tab=readme-ov-file#-prerequisites)
- [Installation](https://github.com/NIKOLENKO-TE/read_random_email/?tab=readme-ov-file#-installation)
- [Usage](https://github.com/NIKOLENKO-TE/read_random_email/?tab=readme-ov-file#-usage)
- [Examples](https://github.com/NIKOLENKO-TE/read_random_email/?tab=readme-ov-file#check-inbox-for-new-emails)
- [Development](https://github.com/NIKOLENKO-TE/read_random_email/?tab=readme-ov-file#%EF%B8%8F-development)

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

### Retrieve a New Email Address

```java
public class Main {
    public static void main(String[] args) {
        String email = GuerrillaMailAPI.getEmailAddress();
        System.out.println("Generated Email: " + email);
    }
}
```

### Check Inbox for New Emails
```java
public class Main {
    public static void main(String[] args) {
        String emailAddress = GuerrillaMailAPI.getEmailAddress();
        GuerrillaMailAPI.readMail(emailAddress, 10, 5, 20, "example.com");
    }
}
```

### Fetch Email Content
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
#### Example 1: Generate and Print an Email Address
```java
public class Main {
    public static void main(String[] args) {
        String email = GuerrillaMailAPI.getEmailAddress();
        System.out.println("Generated Email: " + email);
    }
}

```

#### Example 2: Read Emails with Specific Stop Domain
```java
public class Main {
    public static void main(String[] args) {
        String emailAddress = GuerrillaMailAPI.getEmailAddress();
        GuerrillaMailAPI.readMail(emailAddress, 1, 50, 5, "stop@email.com");
    }
}

```

## 🛠️ Development
#### Setup
* Ensure you have **Java** and **Gradle** installed.
* Clone the repository.
* Import the project into your preferred IDE.
* Running Tests
The project includes a simple test to demonstrate the usage of the API.

```java
import org.junit.jupiter.api.Test;

public class GuerrillaMailAPITest {
    @Test
    public void testReadMail() {
        GuerrillaMailAPI.readMail(GuerrillaMailAPI.getEmailAddress(), 1, 50, 5, "example.com");
    }
}

```
<hr>

## 💡 Feel free to contribute to this project by submitting a pull request or opening an issue! Happy coding! 😊
