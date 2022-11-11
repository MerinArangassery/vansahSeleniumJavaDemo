package utility;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.http.HttpHost;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.json.JSONObject;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Augmenter;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

public class VansahNode {

	//--------------------------- ENDPOINTS -------------------------------------------------------------------------------
	private static final String API_VERSION     = "v1";
	private static final String VANSAH_URL      = "https://prod.vansahnode.app";
	private static final String ADD_TEST_RUN    = VANSAH_URL + "/api/" + API_VERSION+ "/run";
	private static final String ADD_TEST_LOG    = VANSAH_URL + "/api/" + API_VERSION+ "/logs";
	private static final String UPDATE_TEST_LOG = VANSAH_URL + "/api/" + API_VERSION+ "/logs/";
	private static final String REMOVE_TEST_LOG = VANSAH_URL + "/api/" + API_VERSION+ "/logs/";
	private static final String REMOVE_TEST_RUN = VANSAH_URL + "/api/" + API_VERSION+ "/run/";
	private static final String TEST_SCRIPT     = VANSAH_URL + "/api/" + API_VERSION+ "/testCase/list/testScripts";
	//--------------------------------------------------------------------------------------------------------------------


	//--------------------------- INFORM YOUR UNIQUE VANSAH TOKEN HERE ---------------------------------------------------
	private static final String VANSAH_TOKEN = "Your Token Here";
	//--------------------------------------------------------------------------------------------------------------------


	//--------------------------- IF YOU ARE USING VANSAH BINDING BEHIND A PROXY, INFORM THE DETAILS HERE ----------------
	private static final String hostAddr = "";
	private static final String portNo = "";
	//--------------------------------------------------------------------------------------------------------------------	


	//--------------------------- INFORM IF YOU WANT TO UPDATE VANSAH HERE -----------------------------------------------
	// 0 = NO RESULTS WILL BE SENT TO VANSAH
	// 1 = RESULTS WILL BE SENT TO VANSAH
	private static final String updateVansah = "1";
	//--------------------------------------------------------------------------------------------------------------------	


	//--------------------------------------------------------------------------------------------------------------------
	private String TESTFOLDERS_ID;  //Mandatory (GUID Test folder Identifer) Optional if issue_key is provided
	private String JIRA_ISSUE_KEY;  //Mandatory (JIRA ISSUE KEY) Optional if Test Folder is provided
	private String SPRINT_KEY; //Mandatory (SPRINT KEY)
	private String CASE_KEY;   //CaseKey ID (Example - TEST-C1) Mandatory
	private String RELEASE_KEY;  //Release Key (JIRA Release/Version Key) Mandatory
	private String ENVIRONMENT_KEY; //Enivronment ID from Vansah for JIRA app. (Example SYS or UAT ) Mandatory
	private int RESULT_KEY;    // Result Key such as (Result value. Options: (0 = N/A, 1= FAIL, 2= PASS, 3 = Not tested)) Mandatory
	private boolean SEND_SCREENSHOT;   // true or false If Required to take a screenshot of the webPage that to be tested.
	private String COMMENT;  //Actual Result 	
	private int STEP_ORDER;   //Test Step index	
	private String TEST_RUN_IDENTIFIER; //To be generated by API request
	private String TEST_LOG_IDENTIFIER; //To be generated by API request
	private String FILE;
	private File image;
	private HttpClientBuilder clientBuilder;
	private CredentialsProvider credsProvider;
	private HashMap<Integer, String> testSteps = new HashMap<Integer, String>();
	private HashMap<Integer, String> testResults = new HashMap<Integer, String>();
	private List<Integer> listOfSteps;
	private int testRows;
	private Map<String, String> headers = new HashMap<>();
	private String PROJECT_KEY;
	private HashMap<String,Integer> resultAsName = new HashMap<>();



	private JSONObject requestBody = null;

	/**
	 * @param tESTFOLDERS_ID
	 * @param jIRA_ISSUE_KEY
	 * @param sPRINT_KEY
	 * @param rELEASE_KEY
	 * @param eNVIRONMENT_KEY
	 */
	//------------------------ VANSAH INSTANCE CREATION---------------------------------------------------------------------------------
	//Creates an Instance of vansahnode, to set all the required field
	public VansahNode (String testFolder_ID, String jiraIssue, String sprintKey, String release,String environment) {
		super();
		this.TESTFOLDERS_ID = testFolder_ID;
		this.RELEASE_KEY = release;
		this.ENVIRONMENT_KEY = environment;
		this.JIRA_ISSUE_KEY = jiraIssue;
		this.SPRINT_KEY = sprintKey;

		validate(TESTFOLDERS_ID,"TESTFOLDERS_ID");
		validate(JIRA_ISSUE_KEY,"JIRA_ISSUE_KEY");

		//Creating key Object Pair for Test Result
		resultAsName.put("NA",0);
		resultAsName.put("FAILED",1);
		resultAsName.put("PASSED",2);
		resultAsName.put("UNTESTED",3);
	}
	//Validate Vansah Resources before execution
	public void validate(String PROPERTY,String VariableName) {
		if(PROPERTY==null || PROPERTY.length()<=2) {
			System.out.println("Please Provide "+VariableName);
			
		}
	}

	//------------------------ VANSAH ADD TEST RUN(TEST RUN IDENTIFIER CREATION) -------------------------------------------
	//POST prod.vansahnode.app/api/v1/run --> https://apidoc.vansah.com/#0ebf5b8f-edc5-4adb-8333-aca93059f31c
	//creates a new test run Identifier which is then used with the other testing methods: 1) add_test_log 2) remove_test_run

	//For JIRA ISSUES
	public void addTestRunFromJIRAIssue(String testcase) throws Exception {
		String[] project = testcase.split("-");;
		this.PROJECT_KEY = project[0];
		this.CASE_KEY = testcase;
		this.SEND_SCREENSHOT = false;
		validate(CASE_KEY,"CASE_KEY");
		connectToVansahRest("addTestRunFromJIRAIssue", null);
	}

	//For TestFolders
	public void addTestRunFromTestFolder(String testcase) throws Exception {
		String[] project = testcase.split("-");;
		this.PROJECT_KEY = project[0];
		this.CASE_KEY = testcase;
		this.SEND_SCREENSHOT = false;
		validate(CASE_KEY,"CASE_KEY");


		connectToVansahRest("addTestRunFromTestFolder", null);
	}
	//------------------------------------------------------------------------------------------------------------------------



	//-------------------------- VANSAH ADD TEST LOG (LOG IDENTIFIER CREATION ------------------------------------------------
	//POST prod.vansahnode.app/api/v1/logs --> https://apidoc.vansah.com/#8cad9d9e-003c-43a2-b29e-26ec2acf67a7
	//adds a new test log for the test case_key. Requires "test_run_identifier" from add_test_run

	public void addTestLog(int result, String comment, Integer testStepRow, boolean sendScreenShot, WebDriver driver) throws Exception {

		//0 = N/A, 1 = FAIL, 2 = PASS, 3 = Not tested
		this.RESULT_KEY = result;
		this.COMMENT = comment;
		this.STEP_ORDER = testStepRow;
		this.SEND_SCREENSHOT = sendScreenShot;
		validate(COMMENT,"At Least 3 Character Comment");
		connectToVansahRest("addTestLog", driver);
	}
	public void addTestLog(String result, String comment, Integer testStepRow, boolean sendScreenShot, WebDriver driver) throws Exception {

		//0 = N/A, 1 = FAILED, 2 = PASSED, 3 = UNTESTED

		this.RESULT_KEY = resultAsName.get(result.toUpperCase());
		this.COMMENT = comment;
		this.STEP_ORDER = testStepRow;
		this.SEND_SCREENSHOT = sendScreenShot;
		validate(COMMENT,"At Least 3 Character Comment");
		connectToVansahRest("addTestLog", driver);
	}
	//-------------------------------------------------------------------------------------------------------------------------



	//------------------------- VANSAH ADD QUICK TEST --------------------------------------------------------------------------
	//POST prod.vansahnode.app/api/v1/run --> https://apidoc.vansah.com/#0ebf5b8f-edc5-4adb-8333-aca93059f31c
	//creates a new test run and a new test log for the test case_key. By calling this endpoint, 
	//you will create a new log entry in Vansah with the respective overal Result. 
	//(0 = N/A, 1= FAIL, 2= PASS, 3 = Not Tested). Add_Quick_Test is useful for test cases in which there are no steps in the test script, 
	//where only the overall result is important.

	//For JIRA ISSUES
	public void addQuickTestFromJiraIssue(String testcase, int result,String comment, boolean sendScreenShot, WebDriver driver) throws Exception {

		//0 = N/A, 1= FAIL, 2= PASS, 3 = Not tested
		this.CASE_KEY = testcase;
		this.RESULT_KEY = result;
		this.COMMENT = comment;
		this.SEND_SCREENSHOT = sendScreenShot;
		validate(CASE_KEY,"CASE_KEY");
		validate(COMMENT,"At Least 3 Character Comment");
		connectToVansahRest("addQuickTestFromJiraISSUE", driver);
	}
	//For TestFolders
	public void addQuickTestFromTestFolders(String testcase, int result,String comment, boolean sendScreenShot, WebDriver driver) throws Exception {

		//0 = N/A, 1= FAIL, 2= PASS, 3 = Not tested
		this.CASE_KEY = testcase;
		this.RESULT_KEY = result;
		this.COMMENT = comment;
		this.SEND_SCREENSHOT = sendScreenShot;
		validate(CASE_KEY,"CASE_KEY");
		validate(COMMENT,"At Least 3 Character Comment");
		connectToVansahRest("addQuickTestFromTestFolders", driver);
	}

	//------------------------------------------------------------------------------------------------------------------------------


	//------------------------------------------ VANSAH REMOVE TEST RUN *********************************************
	//POST prod.vansahnode.app/api/v1/run/{{test_run_identifier}} --> https://apidoc.vansah.com/#2f004698-34e9-4097-89ab-759a8d86fca8
	//will delete the test log created from add_test_run or add_quick_test

	public void removeTestRun() throws Exception {
		validate(TEST_RUN_IDENTIFIER,"TEST_RUN_IDENTIFIER");
		connectToVansahRest("removeTestRun", null);
	}
	//------------------------------------------------------------------------------------------------------------------------------

	//------------------------------------------ VANSAH REMOVE TEST LOG *********************************************
	//POST remove_test_log https://apidoc.vansah.com/#789414f9-43e7-4744-b2ca-1aaf9ee878e5
	//will delete a test_log_identifier created from add_test_log or add_quick_test

	public void removeTestLog() throws Exception {	
		validate(TEST_LOG_IDENTIFIER,"TEST_LOG_IDENTIFIER");
		connectToVansahRest("removeTestLog", null);
	}
	//------------------------------------------------------------------------------------------------------------------------------


	//------------------------------------------ VANSAH UPDATE TEST LOG ------------------------------------------------------------
	//POST update_test_log https://apidoc.vansah.com/#ae26f43a-b918-4ec9-8422-20553f880b48
	//will perform any updates required using the test log identifier which is returned from add_test_log or add_quick_test

	public void updateTestLog(int result, String comment, boolean sendScreenShot, WebDriver driver) throws Exception {

		//0 = N/A, 1= FAIL, 2= PASS, 3 = Not tested
		this.RESULT_KEY = result;
		this.COMMENT = comment;
		this.SEND_SCREENSHOT = sendScreenShot;
		validate(COMMENT,"At Least 3 Character Comment");
		connectToVansahRest("updateTestLog", driver);
	}
	//----------------------------------------------VANSAH GET TEST SCRIPT-----------------------------------------------------------
	//GET test_script https://apidoc.vansah.com/#91fe16a8-b2c4-440a-b5e6-96cb15f8e1a3
	//Returns the test script for a given case_key

	public int testStepCount(String case_key) {
		
		validate(case_key,"CASE_KEY"); 
		
		try {
			headers.put("Authorization",VANSAH_TOKEN);
			headers.put("Content-Type","application/json");

			clientBuilder = HttpClientBuilder.create();
			// Detecting if the system using any proxy setting.
			
			
			if (hostAddr.equals("") && portNo.equals("")) {
				//System.out.println("No proxy");
				Unirest.setHttpClient(clientBuilder.build());
			} else {
				System.out.println("Proxy Server");
				credsProvider = new BasicCredentialsProvider();
				clientBuilder.useSystemProperties();
				clientBuilder.setProxy(new HttpHost(hostAddr, Integer.parseInt(portNo)));
				clientBuilder.setDefaultCredentialsProvider(credsProvider);
				clientBuilder.setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy());
				Unirest.setHttpClient(clientBuilder.build());
			}
			HttpResponse<JsonNode> get;
			get = Unirest.get(TEST_SCRIPT).headers(headers).queryString("caseKey", case_key).asJson();
			if (get.getBody().toString().equals("[]")) {
				System.out.println("Unexpected Response From Server: " + get.getBody().toString());
			} else {
				JSONObject jsonobjInit = new JSONObject(get.getBody().toString());
				boolean success = jsonobjInit.getBoolean("success");
				String vansah_message = jsonobjInit.getString("message");

				if (success) {

					int testRows = jsonobjInit.getJSONArray("data").length();
					System.out.println("NUMBER OF STEPS: " + testRows);
					return testRows;

				} else {
					System.out.println("Error - Response From Vansah: " + vansah_message);
					return 0;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return 0;

	}

	//******************** MAIN METHOD THAT CONNECTS TO VANSAH (CENTRAL PLACE FOR QUICK TEST AND QUICK TEST UPDATE) ******************************************
	private void connectToVansahRest(String type, WebDriver driver) {

		//Define Headers here
		headers.put("Authorization",VANSAH_TOKEN);
		headers.put("Content-Type","application/json");
		HttpResponse<JsonNode> jsonRequestBody = null;

		if (updateVansah.equals("1")) {

			try {
				clientBuilder = HttpClientBuilder.create();
				// Detecting if binder is being used behind any proxy setting.
				if (hostAddr.equals("") && portNo.equals("")) {
					Unirest.setHttpClient(clientBuilder.build());
				} else {
					System.out.println("Proxy Server");credsProvider = new BasicCredentialsProvider();
					clientBuilder.useSystemProperties();clientBuilder.setProxy(new HttpHost(hostAddr, Integer.parseInt(portNo)));
					clientBuilder.setDefaultCredentialsProvider(credsProvider); clientBuilder.setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy());
					Unirest.setHttpClient(clientBuilder.build());
				}

				if (SEND_SCREENSHOT) {
					try {
						System.out.print("Taking screenshot...");
						WebDriver augmentedDriver = new Augmenter().augment(driver);
						image = ((TakesScreenshot) augmentedDriver).getScreenshotAs(OutputType.FILE);
						String encodstring = encodeFileToBase64Binary(image);
						//FILE = "data:image/png;base64," + encodstring;
						FILE = encodstring;
						System.out.println("Screenshot succesfully taken.");
					} catch (Exception e) {
						System.out.println("Taking Screenshot failed: " + e.toString());
					}
				}


				if(type == "addTestRunFromJIRAIssue") {

					requestBody = new JSONObject("{\r\n"
							+ "    \"case\": {\r\n"
							+ "        \"key\": \""+CASE_KEY+"\"\r\n"
							+ "    },\r\n"
							+ "    \"asset\": {\r\n"
							+ "        \"type\": \"issue\",\r\n"
							+ "        \"key\": \""+JIRA_ISSUE_KEY+"\"\r\n"
							+ "    },\r\n"
							+ "    \"properties\": {\r\n"
							+ "        \"environment\": {\r\n"
							+ "            \"name\": \""+ENVIRONMENT_KEY+"\"\r\n"
							+ "        },\r\n"
							+ "        \"release\": {\r\n"
							+ "            \"name\" : \""+RELEASE_KEY+"\"\r\n"
							+ "        },\r\n"
							+ "        \"sprint\": {\r\n"
							+ "            \"name\" : \""+SPRINT_KEY+"\"\r\n"
							+ "        }\r\n"
							+ "    },\r\n"
							+ "     \"project\" :{\r\n"
							+ "        \"key\":\""+PROJECT_KEY+"\"\r\n"
							+ "    }"
							+ "}");

					//System.out.println(requestBody);
					jsonRequestBody = Unirest.post(ADD_TEST_RUN).headers(headers).body(requestBody).asJson();
					//					System.out.println(jsonRequestBody.getStatus());
				}
				if(type == "addTestRunFromTestFolder") {
					requestBody = new JSONObject("{\r\n"
							+ "    \"case\": {\r\n"
							+ "        \"key\": \""+CASE_KEY+"\"\r\n"
							+ "    },\r\n"
							+ "    \"asset\": {\r\n"
							+ "        \"type\": \"folder\",\r\n"
							+ "        \"identifier\": \""+TESTFOLDERS_ID+"\"\r\n"
							+ "    },\r\n"
							+ "    \"properties\": {\r\n"
							+ "        \"environment\": {\r\n"
							+ "            \"name\": \""+ENVIRONMENT_KEY+"\"\r\n"
							+ "        },\r\n"
							+ "        \"release\": {\r\n"
							+ "            \"name\" : \""+RELEASE_KEY+"\"\r\n"
							+ "        },\r\n"
							+ "        \"sprint\": {\r\n"
							+ "            \"name\" : \""+SPRINT_KEY+"\"\r\n"
							+ "        }\r\n"
							+ "    },\r\n"
							+ "     \"project\" :{\r\n"
							+ "        \"key\":\""+PROJECT_KEY+"\"\r\n"
							+ "    }"
							+ "}");

					//					System.out.println(requestBody);
					jsonRequestBody = Unirest.post(ADD_TEST_RUN).headers(headers).body(requestBody).asJson();
					//					System.out.println(jsonRequestBody.getStatus());
				}


				if(type == "addTestLog") {
					String filename = "";
					long millis = System.currentTimeMillis();
					String datetime = new Date().toGMTString();
					datetime = datetime.replace(" ", "");
					datetime = datetime.replace(":", "");
					String rndchars = RandomStringUtils.randomAlphanumeric(16);
					filename = rndchars + "_" + datetime + "_" + millis;
					if(SEND_SCREENSHOT) {
					requestBody = new JSONObject("{\r\n"
							+ "	\"run\": {\r\n"
							+ "		\"identifier\": \""+TEST_RUN_IDENTIFIER+"\"\r\n"
							+ "	},\r\n"
							+ "	\"step\": {\r\n"
							+ "		\"number\": \""+STEP_ORDER+"\"\r\n"
							+ "	},\r\n"
							+ "	\"result\": {\r\n"
							+ "		\"id\": "+RESULT_KEY+"\r\n"
							+ "	},\r\n"
							+ "	\"actualResult\": \""+COMMENT+"\",\r\n"
							+ "     \"project\" :{\r\n"
							+ "        \"key\":\""+PROJECT_KEY+"\"\r\n"
							+ "    },\r\n"
							+ "     \"attachments\" : [\r\n"
							+ "		{ "
							+ "		\"name\" : "+filename+","
							+ "     \"extension\":\"png\",\r\n"
							+ "		\"file\":\""+FILE+"\"\r\n"
							+ "}]"
							+ "}");
					}
					else {
						requestBody = new JSONObject("{\r\n"
								+ "	\"run\": {\r\n"
								+ "		\"identifier\": \""+TEST_RUN_IDENTIFIER+"\"\r\n"
								+ "	},\r\n"
								+ "	\"step\": {\r\n"
								+ "		\"number\": \""+STEP_ORDER+"\"\r\n"
								+ "	},\r\n"
								+ "	\"result\": {\r\n"
								+ "		\"id\": "+RESULT_KEY+"\r\n"
								+ "	},\r\n"
								+ "	\"actualResult\": \""+COMMENT+"\",\r\n"
								+ "     \"project\" :{\r\n"
								+ "        \"key\":\""+PROJECT_KEY+"\"\r\n"
								+ "    }\r\n"
								+ "}");
					}
					//System.out.println(requestBody);
					
					jsonRequestBody = Unirest.post(ADD_TEST_LOG).headers(headers).body(requestBody).asJson();
				}


				if(type == "addQuickTestFromJiraISSUE") {

					requestBody = new JSONObject("{\r\n"
							+ "    \"case\": {\r\n"
							+ "        \"key\": \""+CASE_KEY+"\"\r\n"
							+ "    },\r\n"
							+ "    \"asset\": {\r\n"
							+ "        \"type\": \"issue\",\r\n"
							+ "        \"key\": \""+JIRA_ISSUE_KEY+"\"\r\n"
							+ "    },\r\n"
							+ "    \"properties\": {\r\n"
							+ "        \"environment\": {\r\n"
							+ "            \"name\": \""+ENVIRONMENT_KEY+"\"\r\n"
							+ "        },\r\n"
							+ "        \"release\": {\r\n"
							+ "            \"name\" : \""+RELEASE_KEY+"\"\r\n"
							+ "        },\r\n"
							+ "        \"sprint\": {\r\n"
							+ "            \"name\" : \""+SPRINT_KEY+"\"\r\n"
							+ "        }\r\n"
							+ "    },\r\n"
							+ "     \"project\" :{\r\n"
							+ "        \"key\":\""+PROJECT_KEY+"\"\r\n"
							+ "    },\r\n"
							+ "      \"result\": {\r\n"
							+ "        \"id\": \""+RESULT_KEY+"\"\r\n"
							+ "    }"
							+ "}");

					//					System.out.println(requestBody);
					jsonRequestBody = Unirest.post(ADD_TEST_RUN).headers(headers).body(requestBody).asJson();
				}
				if(type == "addQuickTestFromTestFolders") {
					requestBody = new JSONObject("{\r\n"
							+ "    \"case\": {\r\n"
							+ "        \"key\": \""+CASE_KEY+"\"\r\n"
							+ "    },\r\n"
							+ "    \"asset\": {\r\n"
							+ "        \"type\": \"folder\",\r\n"
							+ "        \"identifier\": \""+TESTFOLDERS_ID+"\"\r\n"
							+ "    },\r\n"
							+ "    \"properties\": {\r\n"
							+ "        \"environment\": {\r\n"
							+ "            \"name\": \""+ENVIRONMENT_KEY+"\"\r\n"
							+ "        },\r\n"
							+ "        \"release\": {\r\n"
							+ "            \"name\" : \""+RELEASE_KEY+"\"\r\n"
							+ "        },\r\n"
							+ "        \"sprint\": {\r\n"
							+ "            \"name\" : \""+SPRINT_KEY+"\"\r\n"
							+ "        }\r\n"
							+ "    },\r\n"
							+ "     \"project\" :{\r\n"
							+ "        \"key\":\""+PROJECT_KEY+"\"\r\n"
							+ "    },\r\n"
							+ "      \"result\": {\r\n"
							+ "        \"id\": \""+RESULT_KEY+"\"\r\n"
							+ "    }"
							+ "}");

					//					System.out.println(requestBody);
					jsonRequestBody = Unirest.post(ADD_TEST_RUN).headers(headers).body(requestBody).asJson();
				}


				if(type == "removeTestRun") {
					jsonRequestBody = Unirest.delete(REMOVE_TEST_RUN+TEST_RUN_IDENTIFIER).headers(headers).asJson();
				}


				if(type == "removeTestLog") {
					jsonRequestBody = Unirest.delete(REMOVE_TEST_LOG+TEST_LOG_IDENTIFIER).headers(headers).asJson();
				}


				if(type == "updateTestLog") {
					requestBody = new JSONObject("{\r\n"
							+ "    \"result\": {\r\n"
							+ "        \"id\": \""+RESULT_KEY+"\"\r\n"
							+ "    },\r\n"
							+ "    \"actualResult\": \""+COMMENT+"\"\r\n"
							+ "}");
					jsonRequestBody = Unirest.put(UPDATE_TEST_LOG+TEST_LOG_IDENTIFIER).headers(headers).body(requestBody).asJson();
				}


				JSONObject fullBody = jsonRequestBody.getBody().getObject();
				if (jsonRequestBody.getBody().toString().equals("[]")) {
					System.out.println("Unexpected Response From Vansah with empty response: " + jsonRequestBody.getBody().toString());


				} else {
					JSONObject jsonobjInit = new JSONObject(jsonRequestBody.getBody().toString());
					boolean success = jsonobjInit.getBoolean("success");
					String vansah_message = jsonobjInit.getString("message");
					System.out.println("(" + StringUtils.capitalize(type) + ") Return: " + success + ". Vansah Message: " + vansah_message );

					if (success){

						if(type == "addTestRunFromJIRAIssue") {
							TEST_RUN_IDENTIFIER = fullBody.getJSONObject("data").getJSONObject("run").get("identifier").toString();
							System.out.println("Test Run Identifier: " + TEST_RUN_IDENTIFIER);
						}
						if(type == "addTestRunFromTestFolder") {
							TEST_RUN_IDENTIFIER = fullBody.getJSONObject("data").getJSONObject("run").get("identifier").toString();
							System.out.println("Test Run Identifier: " + TEST_RUN_IDENTIFIER);
						}

						if(type == "addTestLog") {
							TEST_LOG_IDENTIFIER = fullBody.getJSONObject("data").getJSONObject("log").get("identifier").toString();
							System.out.println("Test Log Identifier: " + TEST_LOG_IDENTIFIER);
						}

					}else{
						System.out.println("Response From Vansah: " + vansah_message);					
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	//*******************************************************************************************************************

	public int getNumberOfTestRows() {
		return testRows;
	}

	public HashMap<Integer, String> getTestSteps() {
		return testSteps;
	}

	//To capture a Screenshot
	private static String encodeFileToBase64Binary(File file) {
		String encodedfile = null;
		try {
			@SuppressWarnings("resource")
			FileInputStream fileInputStreamReader = new FileInputStream(file);
			byte[] bytes = new byte[(int) file.length()];
			fileInputStreamReader.read(bytes);
			encodedfile = Base64.getEncoder().encodeToString(bytes);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return encodedfile;
	}

}