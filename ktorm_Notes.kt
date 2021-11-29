Official Website : https://www.ktorm.org/

Add Dependency  
      
  For Ktorm 
            https://mvnrepository.com/artifact/org.ktorm/ktorm-core 
            implementation group: 'org.ktorm', name: 'ktorm-core', version: '3.4.1'

  For MySQL : 
            https://mvnrepository.com/artifact/mysql/mysql-connector-java
            implementation group: 'mysql', name: 'mysql-connector-java', version: '8.0.27'


Connect to Database
//Make sure you create DB in phpmyadmin and run project and if Project connect to DB then you will see following info in Console
val database = Database.connect(
        url = "jdbc:mysql://localhost/test_ktorm",
        driver = "com.mysql.cj.jdbc.Driver",
        user = "root",
        password = "")
    //org.ktorm.database - Connected to jdbc:mysql://localhost/test_ktorm, productName: MySQL, productVersion: 5.5.5-10.4.21-MariaDB, logger: org.ktorm.logging.Slf4jLoggerAdapter@6bc28a83, dialect: org.ktorm.database.SqlDialectKt$detectDialectImplementation$1@324c64cd


