module.exports = new function() {
	/*
	local values used for defining the required properties (IE: you can change this section to 
	taste by adding or removing values if desired)

	Example:
	var baseDir = "/Users/ocyrus/dev";
	var tiDir = baseDir + "/appcelerator/git/titanium_mobile";
	*/
	var baseDir = "";
	var tiDir = "";


	// required values
	/*******************************************************************************/

	// Example: this.androidSdkDir = baseDir + "/installed/android-sdk-mac_x86"
	this.androidSdkDir = ""; // location of the android SDK;

	// Example: this.tiSdkDir = tiDir + "/dist/mobilesdk/osx/2.1.0";
	this.tiSdkDir = ""; // location of titanium SDK;

	/*
	this can be changed but shouldn't need to be. This is the location where the harness instances 
	and log output is stored under
	*/
	this.tempDir = "/tmp/driver";

	this.maxLogs = 20; // change this to control how many log files are kept per platform

	/*
	change this in the case you normally want a different logging level (can be "quiet", 
	"normal" or "verbose")
	*/
	this.defaultLogLevel = "normal";

	// port that socket based test runs will use for communication between driver and harness
	this.socketPort = 40404;

	// max number of connection attempts (driver to harness) for socket based test runs
	this.maxSocketConnectAttempts = 20;

	// port that the driver will listen on for http based test runs
	this.httpPort = 8125;

	/*
	if no timeout value is set in a suite file for a specific test, this value will be used as 
	a timeout value for the test
	*/
	this.defaultTestTimeout = 10000;
}
