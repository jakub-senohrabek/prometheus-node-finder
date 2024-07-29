# Prometheus Node Finder
Application for automatically updating prometheus targets file.

To configure the application, use environment variable `DISCOVERY_CONFIG` with such format:
```json5
{
  "outputFile": "targets.json", // output targets file path
  "checkEverySeconds": 10, // check interval in seconds
  "jobs": {
    "jobName": { // prometheus job name
      "type": "fargate",
      "cluster": "my-cluster", // name of the cluster
      "targetPort": 9999 // metrics port of the target application 
    }
  }
}
```
At the moment only fargate detection is supported. This application should run alongside prometheus so that it can access the targets file.

Intended usage is running as side-cart inside the same container as prometheus does.