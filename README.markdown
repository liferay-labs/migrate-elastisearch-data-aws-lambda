# AWS Lambda for listing Elasticsearch backups

  A parameter of type `MigrationRequest` must be provided to the Lambda.

```json
{
 "originHost": "origin endpoint of the aws elasticsearch domain",
 "destinationHost": "destination endpoint of the aws elasticsearch domain",
 "originIndicesPrefix": "Origin indices prefix",
 "destinationIndicesPrefix": "Destination indices prefix",
 "bucket": "Snapshot repository name",
 "role": "IAM Role"
}
```