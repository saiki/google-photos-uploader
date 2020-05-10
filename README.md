google-photos-uploader
======================

Requirements:
-------------
java

Install:
--------
mvn package

Usage:
--------
java -Dcredential=/path/to/secret.json -Drecursive=true -jar target/google-photos-uploader-1.0-SNAPSHOT-jar-with-dependencies.jar username@google.com /path/to/image/dir

-Dcredentail
  OAuth client ID secret json

-Drecursive
  directory walk recursive

License:
--------
MIT

Author:
-------
saiki
