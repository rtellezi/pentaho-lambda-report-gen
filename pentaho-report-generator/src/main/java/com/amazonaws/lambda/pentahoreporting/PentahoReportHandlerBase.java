/**
 * 
 */
package com.amazonaws.lambda.pentahoreporting;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;

/**
 * @author Jaret
 *
 */
public class PentahoReportHandlerBase {
	protected static final String PARM_REPORT = "report";
	protected static final String PARM_OUTPUT_TYPE = "outputType";
	protected static final String PARM_OUTPUT_BUCKET = "folder";
	protected static final String PARM_OUTPUT_KEY = "file";
	protected static final String ENV_S3_BUCKET = "s3_bucket";
	protected static final String PROP_PRPT = "prpt";
	protected static final String PROP_DATA_DRIVER = "dataDriver";
	protected static final String PROP_DATA_URL = "dataUrl";
	protected static final String PROP_DATA_USER = "dataUser";
	protected static final String PROP_DATA_PASSWORD = "dataPassword";
	protected static final String PROP_DATA_QUERY = "dataQuery";
	protected static final String PARM_OUTPUT_TYPE_PDF = "pdf";
	protected static final String PARM_OUTPUT_TYPE_EXCEL = "excel";
	protected static final String PARM_OUTPUT_TYPE_HTML = "html";
	
	protected PutObjectResult putS3Object(String bucketName, String key, InputStream inputStream) {
		// Read out the stream to get the content length which is required by S3
		// WARNING: This could cause OOM errors if not enough memory is allocated to the lambda
		// function
		try {
			byte[] inputBytes = IOUtils.toByteArray(inputStream);
			return putS3Object(bucketName, key, new ByteArrayInputStream(inputBytes), inputBytes.length);
		} catch (IOException e) {
			System.out.println("Caught an Exception while determining the size of the file to upload to S3.");
			System.out.println("Error Message: " + e.getMessage());
		}
		return null;
	}
	
	protected PutObjectResult putS3Object(String bucketName, String key, InputStream inputStream, long contentLength) {
		AmazonS3 s3Client = AmazonS3Client.builder().build();
		try {
			System.out.println("Uploading an object");
			ObjectMetadata omd = new ObjectMetadata();
			return s3Client.putObject(bucketName, key, inputStream, omd);
		} catch (AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException, which" + " means your request made it "
					+ "to Amazon S3, but was rejected with an error response" + " for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which means" + " the client encountered "
					+ "an internal error while trying to " + "communicate with S3, "
					+ "such as not being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		}
		return null;
	}

    protected Map<String, Object> parseParameters(String input) throws IOException, ParseException {
    	Map<String, Object> out = new HashMap<String, Object>();

    	JSONParser parser = new JSONParser();

        JSONObject event = (JSONObject)parser.parse(input);
        if (event.get("queryStringParameters") != null) {
            JSONObject qps = (JSONObject)event.get("queryStringParameters");
            for (Object key : qps.keySet()) {
            	out.put((String) key, (String)qps.get(key));
            }
        }

        if (event.get("pathParameters") != null) {
            JSONObject pps = (JSONObject)event.get("pathParameters");
            for (Object key : pps.keySet()) {
            	out.put((String) key, (String)pps.get(key));
            }
        }

    	return out;
    }
}
