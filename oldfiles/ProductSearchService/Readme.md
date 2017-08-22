### Overview

This is an intermediate REST interface module which redirects the REST requests to the Legacy Backend Layer. This intermediate layer is introduced so as to absorb any future changes to either the UI layer or the backend layer. 

The ProductSearchService exposes the following interfaces

/Product/?categoryId=xx

/Product/{id}

/Category

/Category/id
<br><br>
<b>Build WAR using Maven</b>

- Ensure maven is installed
- On command prompt, navigate to ProductSearchService project
- Run "mvn clean package" command
- ProductSearchService-0.1.war is created under ProductSearchService/target directory
- Use this war file for deploying the ProductSearchService module on app server

<br>
<b>Security Configuration in server.xml</b>
The backend services are secured with basic authentication. Hence the intermediate REST service layer also needs to be secured. Add the following Basic Registry in server.xml file

    <basicRegistry id="basic" realm="BasicRealm"> 
        <!-- <user name="yourUserName" password="" />  -->
        <group name="SecureShopper">
        	<member name="rbarcia"/>
        	<member name="kbrown"/>
        </group>
        <user name="rbarcia" password="{xor}PTNvKDk2LDc="/> <!-- Refer parent documentation for passwords for these users -->
        <user name="kbrown" password="{xor}PTNvKDk2LDc="/> <!-- Refer parent documentation for passwords for these users -->
    </basicRegistry>