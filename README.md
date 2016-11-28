# mod-acquisitions-postgres


Copyright (C) 2016 The Open Library Foundation

This software is distributed under the terms of the Apache License, Version 2.0. See the file ["LICENSE"](
https://github.com/folio-org/mod-acquisitions-postgres/blob/master/LICENSE) for more information.


##### Demo acquisitions module exposing acq apis and objects based on the raml-module-builder framework implemented with async postgres client

This project is built using the [raml-module-builder](https://github.com/folio-org/raml-module-builder), using the postgreSQL async client to implement some basic acquisition APIs. The project also includes a small demo of the drools functionality. It is highly recommended to read the [raml-module-builder README](https://github.com/folio-org/raml-module-builder/blob/master/README.md)

APIs Implemented:

 - Purchase order line CRUD
 - Partial Funds CRUD

Objects / Schemas:

 - Funds
 - Invoices
 - Invoice Lines
 - Purchase Order Lines
 - Vendor

**Important Note**: the raml-module-builder postgres client currently assumes JSONB tables in PostgreSQL.  This is not mandatory and developers can work with regular postgreSQL tables but will need to implement their own data access layer.

Can be run in both embedded postgres mode or with a regular postgres DB server.

## Instructions

clone / download the raml-module-builder and `mvn clean install`

then do the same for the current project `mvn clean install`

To Run:


In order to pre-populate the embedded postgres database so that the currently implemented APIs work correctly, please run the following instead:

`java -jar acq-postgres-json-fat.jar postgres_import_path=[path_to_/src/main/resources/import.sql] -Dhttp.port=8085`


The basic command to start the module (note that the needed tables will not exist in this case):

`java -jar target/acq-postgres-json-fat.jar -Dhttp.port=8082 embed_postgres=true`

Or

`java -jar target/acq-postgres-json-fat.jar -Dhttp.port=8082 db_connection=[postgres-conf.json]`

to run in non-embedded mode - in this case pass the path to a file called postgres-conf.json (see /resources/postgres-conf.json for an example)

```sh

Credentials when running in embedded mode:

port: 6000
host: 127.0.0.1
user: username
password: password

```

Tables referenced:

```sh

create schema test;
create table test.po_line (
  _id SERIAL PRIMARY KEY,
  jsonb JSONB NOT NULL
);

create table test.funds (
  _id SERIAL PRIMARY KEY,
  jsonb JSONB NOT NULL
);

```

### Documentation of the Service's APIs

Documentation is auto-generated from the RAML file into HTML.
After the service is started, the documentation can be viewed at:

http://localhost:8082/apidocs/index.html?raml=raml/acquisitions/acquisitions.raml

### Get Query Examples:

```sh
Make sure to include appropriate headers, as the runtime framework validates them.

Accept: application/json

contains query:

http://localhost:8082/po_lines?query=[{"field":"'po_line_status'","value":{     "value": "SENT", "desc": "sent to vendor"},"op":"@>"}]

regex query:

http://localhost:8082/po_lines?query=[{"field":"'po_line_status'->>'value'","value":"fa(l|t)se","op":"SIMILAR TO"}, {"op":"NOT"}]

simple query:

http://localhost:8082/po_lines

querying on arrays:

http://localhost:8082/po_lines?query=[{"field":"'fund_distributions'->[]->'amount'->>'sum'","value":120,"op":">"}]

multiple constraints (currently default between constraints is AND):

http://localhost:8082/po_lines?query=[[{"field":"'po_line_status'->>'value'","value":"SENT","op":"like"},{"field":"'owner'->>'value'","value":"MITLIBMATH","op":"="},{"op":"AND"}],[{"field":"'po_line_status'->>'value'","value":"SENT","op":"like"}],[{"field":"'rush'","value":"false","op":"="}], [{"field":"'po_line_status'->>'value'","value":"SENT","op":"like"},{"field":"'type'->>'value'","value":"PRINT_ONETIME","op":"="}, {"op":"OR"}]]

comparison query:

http://localhost:8082/po_lines?query=[{"field":"'fund_distributions'->'amount'->>'sum'","value":120,"op":">"}]
```


### Post Query Example:

```sh
http://localhost:8082/po_lines

make sure to include appropriate headers as the runtime framework validates them

Accept: application/json
Content-Type: application/json

body:

{
  "po_line_status": {
    "value": "SENT",
    "desc": "sent to vendor"
  },
  "owner": {
    "value": "MITLIBMATH",
    "desc": "Math Library"
  },
  "type": {
    "value": "PRINT_ONETIME",
    "desc": ""
  },
  "vendor": {
    "value": "YBP",
    "desc": ""
  },
  "vendor_account_CODE": "YBP_CODE",
  "acquisition_method_CODE": {
    "value": "VENDOR_SYSTEM",
    "desc": "Purchased at Vendor System"
  },
  "rush": false,
  "price": {
    "sum": "150.0",
    "po_currency": {
      "value": "USD",
      "desc": "US Dollar"
    }
  },
  "fund_distributions": [
    {
      "fund_code": "12345",
      "amount": {
        "sum": 123.5,
        "currency": "USD"
      }
    }
  ],
  "vendor_reference_number": "ybp-1234567890",
  "ebook_url": "",
  "source_type": "API",
  "po_number": "0987654321",
  "invoice_reference": "",
  "resource_metadata": "/abc/v1/bibs/99113721800121",
  "access_provider": "",
  "material_type": "BOOK",
  "block_alert_on_po_line": [
    {
      "value": "FUNDMISS",
      "desc": "Fund is missing"
    }
  ],
  "note": [],
  "location": [],
  "created_date": "",
  "update_date": "",
  "renewal_period": "",
  "renewal_date": ""
}
```

## Additional information

The [raml-module-builder](https://github.com/folio-org/raml-module-builder) framework.

Other [modules](http://dev.folio.org/source-code/#server-side).

Other FOLIO Developer documentation is at [dev.folio.org](http://dev.folio.org/)
