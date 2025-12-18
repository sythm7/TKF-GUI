# TKF-GUI

To add this library to your project (with Maven) :
- Clone this repository
- Go to the root folder of the cloned repository
- In a terminal, type "mvn install" if maven is installed and is in your path, otherwise open the folder with IntelliJ, add a maven install configuration and run it. 
- Open your own project, go to pom.xml, and add this tag inside the \<dependencies\> tag :
  
  ```
  <dependency>
      <groupId>fr.sythm</groupId>
      <artifactId>tkf-gui</artifactId>
      <version>1.0-SNAPSHOT</version>
  </dependency>
  ```

Reload your Maven project and you're ready to go !
