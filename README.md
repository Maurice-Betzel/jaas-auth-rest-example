A simple example of a REST service using Karaf 3.0.2 and CXF 3.0.2. This example demonstrates a problem using JAAS authentication and then gain access to the underlying security context.

http://karaf.922171.n3.nabble.com/Security-Subject-from-AccessControlContext-is-null-when-using-JAAS-and-CXF-JAASAuthenticationFilter-td4037821.html

Simply install into a Karaf container with CXF 3.0.2 installed.

```sh
  # install cxf
 feature:install -v cxf-jaxrs
 
 # install the bundle
 install -s mvn:com.fleurida.jaas/jaas-auth-rest-example/1.0.0-SNAPSHOT
 ```
 
 Simple test the code that accesses the JAAS Security Context that shows that the underlying security context does not contain a subject:
  ```sh
$ curl -u karaf:karaf -X GET http://localhost:8181/cxf/echo/jaas/t1
{"Token":{"@echo":"t1","@timestamp":"2015-01-19T19:57:58.249+1000","Error":"[subject is null]"}}
  ``` 
    
  Simple test the code that accesses the injected JAXRS Security Context:
  ```sh
$ curl -u karaf:karaf -X GET http://localhost:8181/cxf/echo/jaxrs/t2
{"Token":{"@echo":"t2","@timestamp":"2015-01-19T20:03:00.742+1000","Error":"","Principal":"karaf"}}
  ```


To solve this problem, the `JAASAuthenticationFeature` needs to be added to the cxf bus in addtion to adding the `JAASLoginInterceptor` to the jaxrs bean.

```xml
<cxf:bus>
	<cxf:features>
		<bean class="org.apache.cxf.interceptor.security.JAASAuthenticationFeature">
			<property name="reportFault" value="true" />
		</bean>
	</cxf:features>
</cxf:bus>
```