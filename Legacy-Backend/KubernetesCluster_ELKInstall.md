**Creating Kubernetes Cluster using IBM Bluemix Dashboard and installing ELK for Git Hub**

- Create a Kubernetes cluster with one worker node
- Install the CLIs for using the Kubernetes API and managing Docker images
- Create a private image repository in IBM® Bluemix® Container Registry to store your images

**Pre-requisites:**  Installing the following CLIs.

- Bluemix CLI
- IBM Bluemix Container Service plug-in
- Kubernetes CLI
- Bluemix Container Registry plug-in
- Docker CLI

From the catalog, in the  **Containers**  category, click  **Kubernetes cluster**.

Enter a  **Cluster Name as &quot;dev\_cluster&quot;**. The default cluster type is lite. Next time, you can create a standard cluster and define additional customizations, like how many worker nodes are in the cluster.

Click  **Create Cluster**. The details for the cluster open,

but the worker node in the cluster takes a few hours to provision. You can see the status of the worker node in the  **Worker nodes**  tab. When the status reaches Ready, your worker node is ready to be used.

Verify the IBM Bluemix Dashboard  for the status of Kubernetes Cluster &quot;dev\_cluster&quot; .

<img src="images/KubernetesCluster_ELK_image1.png" width="623" height="208" />

Open shell window and execute the following commands to launch Kubernetes Dashboard:

              bx login or bx login –sso

bx target -o sankar@yahoo.com -s dev

bx plugin install container-registry -r Bluemix

bx plugin install container-service -r Bluemix

bx cs init

bx cs clusters

bx cs workers &lt;cluster&gt;

bx cs cluster-config &lt;cluster\_name\_or\_id&gt;

export KUBECONFIG=/Users/&lt;user\_name&gt;/.bluemix/plugins/container-service/clusters/&lt;cluster\_name&gt;/kube-config-prod-dal10-&lt;cluster\_name&gt;.yml

## **Launching the Kubernetes dashboard**

kubectl proxy

        http://localhost:8001/ui

Open the Browser for   [http://localhost:8001/ui](http://localhost:8001/ui)

And Click Deployments and Click Create to enter the details as shown below:

App Name:bmelk

Container Name: jconallen/elk-bluemix

Note: Docker Hub  for IBM Bluemix ELK for Deployment into Kubernetes cluster to get the container name.

Service : External

Port and Target ports:  5000  5000

5601 5601              9200 9200                  9300 9300                  5044 5044

Click Deploy

<img src="images/KubernetesCluster_ELK_image2.png" width="623" height="208" />

Note: Wait for an hour to provision the external ports and deployment completion

Click on the bmelk and under services find the port mapping for public access

<img src="images/KubernetesCluster_ELK_image4.png" width="623" height="208" />

The public ports can be used with public IP address of the dev\_cluster  which is obtained from IBM Bluemix Dashboard.

bmelk:5601 TCP - bmelk:32610 TCP   Kibana

bmelk:9200 TCP - bmelk:32156 TCP   elasticsearch

bmelk:5044 TCP - bmelk:31856 TCP   logstash

bmelk:5000 TCP - bmelk:30015 TCP   syslog

Go back to IBM Bluemix Dashboard and Click the &quot;dev\_cluster&quot;

Select the Worker Nodes by Clicking that

Note down the Public IP Address to access the ELK Logging from the browser.
