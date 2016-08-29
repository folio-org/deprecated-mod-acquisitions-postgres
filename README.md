# acquisitions-postgres
Demo acquisitions module exposing acq apis and objects based on the raml-module-builder framework implemented with async postgres client

This project is built using the [raml-module-builder](https://github.com/folio-org/raml-module-builder), using the postgreSQL async client to implement some basic acquisition APIs. The project also includes a small demo of the drools functionality.

APIs Implemented: 

 - purchase order line CRUD
 - Partial Funds CRUD

Objects / Schemas:

 - Funds 
 - Invoices 
 - Invoice Lines 
 - Purchase Order Lines 
 - Vendor

Can be run in both embedded postgres mode or with a regular postgres DB server 

instructions:

`mvn clean install`

Run:

java -jar acq-postgres-json-fat.jar -Dhttp.port=8082 embed_postgres=true

### Get Query Examples:

```sh
make sure to include appropriate headers as the runtime framework validates them

Authorization: aaaaa
Accept: application/json

contains query:

http://localhost:8082/apis/po_lines?query=[{"field":"'po_line_status'","value":{     "value": "SENT", "desc": "sent to vendor"},"op":"@>"}]

regex query

http://localhost:8082/apis/po_lines?query=[{"field":"'po_line_status'->>'value'","value":"fa(l|t)se","op":"SIMILAR TO"}, {"op":"NOT"}]

simple query

http://localhost:8082/apis/po_lines

querying on arrays

http://localhost:8082/apis/po_lines?query=[{"field":"'fund_distributions'->[]->'amount'->>'sum'","value":120,"op":">"}]

multiple constraints (currently default between constraints is AND)

http://localhost:8082/apis/po_lines?query=[[{"field":"'po_line_status'->>'value'","value":"SENT","op":"like"},{"field":"'owner'->>'value'","value":"MITLIBMATH","op":"="},{"op":"AND"}],[{"field":"'po_line_status'->>'value'","value":"SENT","op":"like"}],[{"field":"'rush'","value":"false","op":"="}], [{"field":"'po_line_status'->>'value'","value":"SENT","op":"like"},{"field":"'type'->>'value'","value":"PRINT_ONETIME","op":"="}, {"op":"OR"}]]

comparison query:

http://localhost:8082/apis/po_lines?query=[{"field":"'fund_distributions'->'amount'->>'sum'","value":120,"op":">"}]
```


### Post Query Example:

```sh
http://localhost:8082/apis/po_lines

make sure to include appropriate headers as the runtime framework validates them

Authorization: aaaaa
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
