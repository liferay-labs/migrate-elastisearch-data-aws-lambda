# AWS Lambda for listing Elasticsearch backups:

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

PROD2DEV: Migrate from PROD to DEV


```json
{
 "originHost": "https://search-pulpo-elasticsearch-prod-t7zkxvse3iizvi3i6rgckxv5ee.us-east-1.es.amazonaws.com/",
 "destinationHost": "https://search-pulpo-elasticsearch-dev-2xp4jucrau2hcqsowbsaf5vnfu.us-east-1.es.amazonaws.com/", 
 "originIndicesPrefix": "prod_",
 "destinationIndicesPrefix": "dev_",
 "bucket": "us-east-1-pulpo-engine-contacts-elasticsearch-migrations",
 "role": "arn:aws:iam::931050637112:role/stack-elasticsearch-migra-ElasticsearchMigrationsR-1T27246N82Q8N"
}
```

DEV2CI: Migrate from DEV to CI

```json
{
  "originHost": "https://search-pulpo-elasticsearch-dev-2xp4jucrau2hcqsowbsaf5vnfu.us-east-1.es.amazonaws.com/",
  "destinationHost": "https://search-pulpo-elasticsearch-ci-24ohek6gmy4rsiqvwtuqexhl6q.us-east-1.es.amazonaws.com/",
  "originIndicesPrefix": "dev_",
  "destinationIndicesPrefix": "ci-migrated-from-dev_",
  "bucket": "us-east-1-pulpo-engine-contacts-elasticsearch-migrations",
  "role": "arn:aws:iam::931050637112:role/stack-elasticsearch-migra-ElasticsearchMigrationsR-1T27246N82Q8N"
}
```