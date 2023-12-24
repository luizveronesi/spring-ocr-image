# Spring Image OCR

Java implementation to OCR services from AWS Textract and Azure Computer Vision.

## Installation

```bash
# Clone the repository
git https://github.com/luizveronesi/spring-ocr-image.git

# Navigate to the project directory
cd spring-ocr-image

# Install dependencies
mvn install
```

```bash
# Docker installation
mvn clean package -f pom.xml -U
docker build . -t spring-ocr-image-example:latest
docker create --name spring-ocr-image-example --network your-network --ip x.x.x.x --restart unless-stopped spring-ocr-image-example:latest bash
docker start spring-ocr-image-example
```

## Usage

```bash
# Run the application
java -jar target/api.jar
```

Open Swagger: http://localhost:8080/swagger-ui/index.html

## Configuration

All configuration parameters must be set at file src/main/resources/application.yml.

### AWS Textract

```bash
app:
  aws:
    region:
      static: us-east-1 # your region
    credentials:
      access-key: ${AWS_ACCESS_KEY}
      secret-key: ${AWS_SECRET_KEY}
    textract:
      endpoint: https://textract.us-east-1.amazonaws.com # this endpoint varies with region
```

### Azure Computer Vision

```bash
app:
  azure:
    computer:
      vision:
        subscriptionKey: ${AZURE_COMPUTER_VISION_KEY}
        endpoint: ${AZURE_COMPUTER_VISION_ENDPOINT}
```

## Endpoint

### POST /

Upload an image and extract its text.

#### Request

| Parameter |     Type      | Description                                                           |
| --------: | :-----------: | --------------------------------------------------------------------- |
|    `file` | MultipartFile | The file itself.                                                      |
|    `type` |    option     | Select the engine to extract the text. Available options: AWS, AZURE. |

#### Response

| Parameter |  Type  | Description                                                                                                                                                                                                                                                                                                                                                                                                              |
| --------: | :----: | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
|  `result` | object | The object from each engine response. If type is AWS, it is an AnalyzeDocumentResult (https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/textract/model/AnalyzeDocumentResult.html). If type is Azure, it is a ReadOperationResult https://learn.microsoft.com/en-us/java/api/com.microsoft.azure.cognitiveservices.vision.computervision.models.readoperationresult?view=azure-java-archive). |
|    `text` | string | The extracted text.                                                                                                                                                                                                                                                                                                                                                                                                      |

## Next steps

Implement unit tests.
