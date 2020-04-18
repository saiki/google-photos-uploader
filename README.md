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
java -Dcredential=/path/to/secret.json -Droot=true -jar target/google-photos-uploader-1.0-SNAPSHOT-jar-with-dependencies.jar /path/to/image/dir username@google.com

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
