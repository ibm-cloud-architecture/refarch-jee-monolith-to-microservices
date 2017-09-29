# Customer Order Services - Microservices based application

This GitHub repository contains the information for the Customer Order Services microservices based application. This application is now composed of several microservices. Best practices for microservices is that each of them have their own GitHub repository and be as independent of any other component as possible. Hence, this repository serves as the umbrella for all the microservices that make up the final Customer Order Services application.

![Phase 4 Application Architecture](https://github.com/ibm-cloud-architecture/refarch-jee-monolith-to-microservices/blob/master/images/purplecompute-architecture.png?raw=true)

## Repositories

-   [Shopping WEB BFF Microservice](https://github.com/ibm-cloud-architecture/refarch-jee-micro-shopping-bff)
-   [Product Search Microservice](https://github.com/ibm-cloud-architecture/refarch-jee-micro-product-service)
-   [Customer Order Microservice](https://github.com/ibm-cloud-architecture/refarch-jee-micro-customer-service)
-   [Legacy Application](Legacy-Backend)


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
