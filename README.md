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
java -jar target/google-photos-uploader-1.0-SNAPSHOT-jar-with-dependencies.jar -D
-Dcredential=/path/to/secret.json -Droot=/path/to/image/dir -Droot=true

-Dcredentail
  OAuth client ID secret json

-Droot
  image root directory

-Drecursive
  directory walk recursive

License:
--------
MIT

Author:
-------
saiki
