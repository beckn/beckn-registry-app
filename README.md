# Beckn Gateway Application
This is  skeletal [succinct](https://github.com/venkatramanm/swf-all) application. that uses the plugin 
[beckn-registry](https://github.com/venkatramanm/beckn-registry). 

To build and install the app locally, 
===
You need to Clone and build the following repositories (using mvn install in each of the directories) in sequence
1. [Common](https://github.com/venkatramanm/common) 
2. [Beckn SDK for Java](https://github.com/venkatramanm/beckn-sdk-java) 
3. [Succinct Web Framework](https://github.com/venkatramanm/swf-all)
4. [Beckn registry plugin](https://github.com/venkatramanm/beckn-registry). 


Then clone [this repo you are reading ](https://github.com/venkatramanm/beckn-registry-app); 

1. Go into the cloned folder. 
    cd beckn-registry-app

2. Copy sample overrideProperties.sample to overrideProperties;
    cp -R overrideProperties.sample overrideProperties 

3. Locate swf.propeties file in overrideProperties and edit the section pertaining to "Beckn Gateway configurations". 

4. from the base directory for beckn-registry-app , run bin/swfstart 


*Your registry will be up.  check logs in tmp/ folder to see if there are any issues and resolve them*






