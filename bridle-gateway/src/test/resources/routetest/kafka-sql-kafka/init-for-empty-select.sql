CREATE TABLE USERS (
  USER_ID VARCHAR2(4) NOT NULL PRIMARY KEY,
  FIRST_NAME VARCHAR2(100) NOT NULL,
  LAST_NAME VARCHAR2(100) NOT NULL,
  EMAIL VARCHAR2(100) NOT NULL,
  PHONE_NUMBER VARCHAR2(20)
);

INSERT INTO USERS (USER_ID, FIRST_NAME, LAST_NAME, EMAIL, PHONE_NUMBER)
VALUES ('5678', 'Jane', 'Smith', 'jane.smith@example.com', '+1 555 765 4321');