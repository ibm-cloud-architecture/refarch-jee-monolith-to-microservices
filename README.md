# Phase 4 – Evolve to Microservices

## Patterns and best practices used

This phase focuses on breaking the monolithic application into microservices and hosting them on kubernetes on Bluemix. The key goal behind breaking it into microservices is to address the current business requirements around agility, faster application/feature roll out, application performance tuning etc. 

The process of breaking a monolith application into microservices is a form of application modernization, which everyone is aware of and has been practiced by industry for a long time. Some of the key approaches from this are

1.	Not to break the complete application in one go in a big bang manner. Rather the application should be broken into microservices gradually and until the time you have completed the process, run it in conjunction with the monolith application. Over time, the amount of functionality implemented by the monolithic application shrinks until either it disappears entirely or it becomes just another microservice. This approach is also called the Strangler pattern.

2.	Break the front end and Backend. This is an approach to split the presentation layer from the business logic and data access layers. Most of the time it is easier to achieve this because of the way typical J2EE applications are developed. 
When you break the presentation layer from the business logic layer, you may need to introduce some additional code to break the direct dependency that may exist because of co-location and direct references. If there is REST service layer already existing (which is the case in this application), it is easy to separate the presentation layer from the monolith and create a separate microservice for the frontend. If there are no REST/Service layer existing, the first step would be to identify and create a REST service layer which will separate the direct dependency between the front end and business logic. 
See the challenges and steps to break and create front end micro service <Link to FrontEnd Microservice>.

3.	Extract Services from the existing monolith. Depending on the business requirements, and the technical feasibility, identify the modules that needs to be separated out as a separate microservice. 

4. Microservices for legacy backend. Sometimes, it may be impossible to modernize the application completely. Hence we may need to create some  microservices that represents the business capability of the backend legacy application. For this application we have created two microservices representing the “CustomerOrderService” and the “ProductSearchService”.  

# Existing Architecture
![J2EE Application Architecture ](https://raw.githubusercontent.com/ibm-cloud-architecture/refarch-jee/master/static/imgs/apparch-pc-phase0-customerorderservices.png)

Please refer to [customer order](https://github.com/ibm-cloud-architecture/refarch-jee-customerorder/tree/was90-dev/README.md) application for getting an overview of the existing application that has been modernized into a microservice based architecture.


# Target Architecture
![Purple Compute Architecture ](images/purplecompute-architecture.png?raw=true "Purple Compute Architecture")

The above monolith application has been broken down into 3 micro services. The micro services are deployed into Kubernetes cluster on Bluemix. Part of the functionality will continue to be in the legacy backend, which may be modernized over a period of time. For the time being it continues to run on Websphere Application Server.

<br />
<br />
# Steps Followed to move from Existing Architecture to Microservice based Target Architecture
 
## Step 1 - Break the front end from Backend 
This is an approach to split the presentation layer from the business logic and data access layers. Most of the time it is easier to achieve this because of the way typical J2EE applications are developed. Here the existing DOJO based frontend has been  moved out from the main J2EE application as a separate Microservice called [Shopping WEB BFF Microservice](https://git.ng.bluemix.net/debasis.das/purplecompute-ShoppingWebBFFService). Currently we have only Web channel through which the request is received. Hence we have created the the BFF (Backend for Frontend) Microservice for Web. In future if we need to cater to mobile channel, we can create another BFF Microservice for mobile to cater to the requirement. Find below the steps and challenges for breaking the frontend from Backend and creating it as a separate Microservice.

#### Shopping WEB BFF Microservice
A new microservice called [Shopping WEB BFF Microservice](https://git.ng.bluemix.net/debasis.das/purplecompute-ShoppingWebBFFService) was created by copying the code from the CustomerOrdersServiceWeb project to recreate the front end. In the existing Web Frontend code, the web application was making direct EJB references to the EJBs as they were running in the same server. When we separate out the front end and make it a separate microservice, we needed to make changes to it. Instead of making direct calls, we had to make REST API calls to the backend micro services. This introduced the [CORS](https://en.wikipedia.org/wiki/Cross-origin_resource_sharing) challenge. The web client is written in Dojo. In Dojo, relative path is used to call a REST interface. And this method is useful for rest interfaces that are implemented/deployed within the same domain. If the REST interfaces are available across domain then the relative path approach does not work. Using JSONP approach is a way to achieve cross domain REST invocations. However for this approach to work, server side changes are required.


What is CORS:
[From Wiki - Cross-origin resource sharing (CORS) is a mechanism that allows restricted resources (e.g. fonts) on a web page to be requested from another domain outside the domain from which the first resource was served.A web page may freely embed cross-origin images, stylesheets, scripts, iframes, and videos. Certain "cross-domain" requests, notably Ajax requests, however are forbidden by default by the same-origin security policy.CORS defines a way in which a browser and server can interact to determine whether or not it is safe to allow the cross-origin request. It allows for more freedom and functionality than purely same-origin requests, but is more secure than simply allowing all cross-origin requests. It is a recommended standard of the W3C](https://en.wikipedia.org/wiki/Cross-origin_resource_sharing)


Hence to overcome this issue Java code is introduced in the same project as the dojo code is in, so that the dojo code can call the interface exposed by java code within the same project and in turn java code can invoke cross domain calls. There are multiple ways CORS issue can be handled. In this case we created another layer of redirection such that the front end makes calls to a routing module deployed along with the front end service, which in turn makes the REST calls to the backend microservices.

#### Steps
1. A microservice was created by copying the existing Web front end code
2. A simple Java class is written to proxy the backend microservices - that the DOJO frontend can invoke (to get around CORS challenge)

## Step 2 – Identify and create Microservices for Backend and apply Strangler pattern
The application was analyzed and modules were identified using domain driven design approach. For the above application we identified two key modules namely 
[CustomerOrder Microservice](https://git.ng.bluemix.net/debasis.das/purplecompute-CustomerOrderService) and [ProductSearch Microservice](https://git.ng.bluemix.net/debasis.das/purplecompute-ProductSearchService) (As represented by "KUBERNETES BACKEND SERVICES" block in the above target architecture diagram). We created two Microservices for each of these two services. Even though currently the microservice only work as service routers, we can use [strangler pattern](https://www.ibm.com/developerworks/cloud/library/cl-strangler-application-pattern-microservices-apps-trs/index.html) over a period of time to move/modernize some of the capabilities from the backend into these microservices. 

E.g. Now the backend services has ProductSearch module. This service could be written separately altogether using new technologies, like nodejs and nosql. For the new APIs to be used, there's only a minor change that needs to be done in ProductSearch Microservice. No changes whatsoever are required in any other module. Similarly it can be extended to any existing APIs and new APIs.

## Step 3 – Breaking the backend application into Separate EARs
Instead of having a monolithic app as a single EAR, it was percived that the application should be broken into logical units so that we can take it towards a microservice architecture and have better control over individual functionalities of the application. Hence the single EAR was broken into two EARS having one EJB each. These EJBs are called CustomerOrder EJB and ProductSearch EJB. 


 - Steps followed to break it into 2 EARs
The multiple EJB  Business functionality included in the same EAR has been split into individual 
JEE EJB packages using the following steps:
•	Create a directory ProductSearchService and copy the CustomerOrderServices content into that. 
•	Delete the ProductSearchService and ProductSearchServiceImpl class from CustomerOrderServices ejbmodule directory.  
•	Delete CustomerOrderServices and CustomerOrderServicesImpl from ProductSearchService ejbmodule directory.
•	There is no change in the utility classes used in both the modules
 
 - Steps followed to expose them to the outside world as Services
The wrapper classes are used to expose the EJB REST API Java classes in the respective Web
Modules CustomerOrderServicesWeb and ProductSearchServiceWeb to access from outside. 
The steps required to do the same are
•	Create a directory ProductSearchServiceWeb and copy the contents of CustomerOrderServicesWeb into it.  
•	Rename the CustomerServicesApp class as ProductSearchServiceApp class for ProductSearchServiceWeb module Java src folder and remove the CustomerOrderResource class in it.
•	Remove the CategoryResource and ProductResource from CustomerOrderServicesWeb.
•	Delete WebContent from both the modules. Modify the pom.xml file accordingly

## Step 4 - Handle Application Security
When you refactor your application into Microservices, your traditional application security mechanism may not work. As a sample implementation, we have used Open LDAP as our directory server and SSO provider, which can federate access to different microservices and pass on the access context appropriately. For the shopping application, we have used Liberty as the implementation runtime. Appropriate changes were made to the server configuruation files (Server.xml) to enable SSO and use OLDAP as the SSO service provider. The detail steps are discussed [here](https://git.ng.bluemix.net/debasis.das/purplecompute-phase4/blob/master/Legacy-Backend/SSO_OpenLDAP_Configuration.md). 



# Getting Started


## Repositories

-   [Shopping WEB BFF Microservice](https://git.ng.bluemix.net/debasis.das/purplecompute-ShoppingWebBFFService)
-   [Product Search Microservice](https://git.ng.bluemix.net/debasis.das/purplecompute-ProductSearchService)
-   [Customer Order Microservice](https://git.ng.bluemix.net/debasis.das/purplecompute-CustomerOrderService)
-   [Legacy Application](Legacy-Backend)

## Steps followed to setup Bluemix
- Steps to create the required Bluemix services (WAS, dashDB, Kubernetes Cluster) etc
- Steps to populate the database


## Steps followed to Build and Deploy the application

### Building and deploying the Backend Application on Websphere Application Server

Follow the steps mentioned [here](Legacy-Backend/README.md) to build and deploy the Backend applications

### Building and Deploying the Microservices on Kubernetes

Follow the below steps to build and deploy the Microservices. 

Continuous Delivery service on Bluemix is used to create end-to-end deployment of 3 microservices on Bluemix Kubernetes Container Service.

#### Steps
	
1. Create a Kubernetes Cluster on Bluemix. Follow steps here : https://console.bluemix.net/docs/containers/cs_tutorials.html#cs_tutorials to create the cluster
2. On Bluemix Dashboard, create a toolchain.
3. Add Github Tool integration and mention git repository URL for CustomerOrderService micro service
4. Add Github Tool integration and mention git repository URL for ProductSearchService micro service
5. Add Github Tool integration and mention git repository URL for ShoppingWebBFFService micro service
6. Add Delivery Pipeline Tool integration and follow below steps to create deployment flow for CustomerOrderService microservice
    1. Click on Delivery Pipeline toll integration which is just added
    2. Add a stage to build the microservice code
    3. In Input tab, select git repository for CustomerOrderService which was added in step 2
    4. Make sure Stage Trigger is set to “Run jobs whenever a change is pushed to Git”
    5. In Jobs tab, Add a Build Job
    6. Select Builder type as “Maven” and Working Directory as “CustomerOrderService” and Save it
    7. Add another stage to build docker image
    8. In Input tab, select Stage and Build from the previous stage added above
    9. Make sure Stage trigger is set to “Run jobs when the previous stage is completed”    
    10. In Jobs tab, add a new Build job
    11. Select Builder type as “IBM Container Service” and select rest of the input parameters appropriately (like Organization, space, etc). Secify image name as customerorderservice.
    12. In Build script, add below script towards the end
	```	
    if [ -f customerorderservice.yaml ]; then
  			#Update customerorderservice.yml with image name
  			echo "UPDATING DEPLOYMENT MANIFEST:"
  			sed -i "s~^\([[:blank:]]*\)image: ${IMAGE_NAME}*$~\1image: ${FULL_REPOSITORY_NAME}~" customerorderservice.yaml
  			cat customerorderservice.yaml
  			cp customerorderservice.yaml $ARCHIVE_DIR/
		else
  			echo -e "${red}Kubernetes deployment file 'customerorderservice.yaml' not found at the repository root${no_color}"
  			exit 1
    	fi
	```
	This script is required to update images name in Kubernetes deployment file with the image which is built in this step. Save it
	13. Add another stage to trigger kubernetes deployment.
    14. In Input tab, select Stage and Job from the previous stage of Container build
    15. Make sure Stage trigger is set to “Run jobs when the previous stage is completed”
    16. Add a new Deploy job.
    17. Select Deployer type as Kubernetes and specify input parameters as appropriate
    18. In deploy script add below lines in place of “kubectl create -f pods.yml” and save

        `kubectl delete -f customerorderservice.yaml`
	    `kubectl create -f customerorderservice.yaml`

6. Add Delivery Pipeline Tool integration and follow below steps to create deployment flow for ProductSearchService microservice. Specify image and filename as “productsearchservice” in step 5.11
7. Add Delivery Pipeline Tool integration and follow below steps to create deployment flow for ShoppingWebBFFService microservice. Specify image and filename as “shoppingwebbffservice” in step 5.11