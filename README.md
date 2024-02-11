# client-credentials-appsso-client

This is a sample Springboot REST API that acts as a SSO Client in a service to service flow using the `client_credentials` grant_type against a TAP AuthServer to call a [secured resource server API](https://github.com/x95castle1/client-credentials-appsso-resource-server).

The application example can request scopes of `developer.read` and `developer.write`, but is not granted the scope of `developer.admin`.

## Getting Started

Before running the workload a few components need to be setup:

### Client Registration

A ClientRegistration needs to be created with the grant_type of `client_credentials`. The AuthServer selected by the ClientRegistration will generate a service binding secret that contains the username and password to authenticate against the AuthServer to retrieve a JWT used by the Client with the appropriately request scopes. The Client can only request scopes that are defined in the ClientRegistration.

```YAML
apiVersion: sso.apps.tanzu.vmware.com/v1alpha1
kind: ClientRegistration
metadata:
  name: appsso-demo-client-credentials
  namespace: dev
spec:
  authServerSelector:
    matchLabels:
      env: production
      name: auth0-authserver
  authorizationGrantTypes:
    - client_credentials
    - refresh_token
  clientAuthenticationMethod: client_secret_basic
  scopes:
    - name: openid
    - name: developer.read
    - name: developer.write
```

Here is a sample secret generated from the ClientRegistraton:

```YAML
apiVersion: v1
data:
authorization-grant-types: client_credentials,refresh_token
client-authentication-method: client_secret_basic   
client-id: xxxxxx
client-secret: xxxxxx
issuer-uri: https://<issuer-uri>    
provider: appsso                                                        
scope: openid,developer.read,developer.write                            
type: oauth2
kind: Secret
metadata:
  name: appsso-demo-client-credentials
  namespace: dev
type: servicebinding.io/oauth2
```

### Resource Claim

A ResourceClaim (or ClaimClaim) is needed to create allow the workload to create a service claim against the ClientRegistration. This will allow the servicebinding secret to be mounted onto the running pod from the Workload and allow Spring Cloud Bindings to inject the values from the secret into the running app. 

```YAML
apiVersion: services.apps.tanzu.vmware.com/v1alpha1
kind: ResourceClaim
metadata:
  name: appsso-demo-client-credentials
  namespace: dev
spec:
  ref:
    apiVersion: sso.apps.tanzu.vmware.com/v1alpha1
    kind: ClientRegistration
    name: appsso-demo-client-credentials
    namespace: dev
```

### Workload

A workload.yaml sample has been provided. You can apply this in your TAP developer namespace to run the sample application. It contains a Service Claim against the Resource Claim.

```YAML
apiVersion: carto.run/v1alpha1
kind: Workload
metadata:
  labels:
    app.kubernetes.io/part-of: appsso-demo-client-credentials-read
    apps.tanzu.vmware.com/workload-type: web
  name: appsso-demo-client-credentials-read
  namespace: dev
spec:
  params:
    - name: annotations
      value:
        autoscaling.knative.dev/minScale: "1"
  build:
    env:
    - name: BP_JVM_VERSION
      value: "17"
  serviceClaims:
  # this name has to match the client registration ID you want to use in the SPRING CONFIG
  - name: appsso-demo-client-credentials
    ref:
      apiVersion: services.apps.tanzu.vmware.com/v1alpha1
      kind: ResourceClaim
      name: appsso-demo-client-credentials-read
  - name: ca-cert
    ref:
      apiVersion: v1 
      kind: Secret
      name: tap-ca 
  source:
    git:
      ref:
        branch: main
      url: https://github.com/x95castle1/client-credentials-appsso-client
```

* Note: The service claim for `tap-ca` is only needed if you are running in an environment with selfsigned or custom certificates for exposed endpoints. The service claim will package those custom certificates into the image to avoid X509 fun. 

### Executing the application

* `/api/test` - Executing this endpoint on the application will attempted to call the resource servers protected endpoint that needs a scope of `developer.read`. 

## Testing

There are two sets of Workloads, ClientRegistrations, and ResourceClaims.

The first will 




