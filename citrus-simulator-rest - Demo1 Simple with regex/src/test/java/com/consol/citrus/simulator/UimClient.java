package com.consol.citrus.simulator;

import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import javax.xml.namespace.QName;

import javax.xml.soap.*;

public class UimClient {
    public static void main(String[] args) {
        System.out.println("Hi, I am the UIM client");
        new UimClient().doCall();
    }

    private void doCall() {

        final String cWSSE = "wsse";
        String cURL = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
        String cNODE_SECURITY = "Security";
        String cNODE_USRTOKEN = "UsernameToken";
        String cNODE_USERNAME = "Username";
        String cNODE_PASSWORD = "Password";
        String iUsername = "uim_admin";
        String iPassword = "****";  // NOTE Put the correct password here!


        try {
            MessageFactory mfactory = MessageFactory.newInstance();
            SOAPMessage request = mfactory.createMessage();
            SOAPHeader tHeader = request.getSOAPHeader();

            SOAPEnvelope tSoapEnvelope = request.getSOAPPart().getEnvelope();


            // UIM uses the OASIS web service security standard. Below are code to set the username and password

            // security node
            Name tWsseHeaderName = tSoapEnvelope.createName(cNODE_SECURITY, cWSSE, cURL);
            SOAPHeaderElement tSecurityElement = tHeader.addHeaderElement(tWsseHeaderName);
            tSecurityElement.setMustUnderstand(true);

            Name tUserTokenElementName = tSoapEnvelope.createName(cNODE_USRTOKEN, cWSSE, cURL);
            SOAPElement tUserTokenElement = tSecurityElement.addChildElement(tUserTokenElementName);
            tUserTokenElement.removeNamespaceDeclaration(cWSSE);
            tUserTokenElement.addNamespaceDeclaration("wsu", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");

            // user name child
            Name tUsernameElementName = tSoapEnvelope.createName(cNODE_USERNAME, cWSSE, cURL);
            SOAPElement tUsernameElement = tUserTokenElement.addChildElement(tUsernameElementName);
            tUsernameElement.removeNamespaceDeclaration(cWSSE);
            tUsernameElement.addTextNode(iUsername);

            // password child
            Name tPasswordElementName = tSoapEnvelope.createName(cNODE_PASSWORD, cWSSE, cURL);
            SOAPElement tPasswordElement = tUserTokenElement.addChildElement(tPasswordElementName);
            tPasswordElement.removeNamespaceDeclaration(cWSSE);
            tPasswordElement.addTextNode(iPassword);
            tPasswordElement.setAttribute("Type", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText");


            // Add the XML-content
            SOAPBody body = request.getSOAPBody();
            QName bodyName = new QName("http://xmlns.oracle.com/communications/inventory/webservice/nsrm", "importEntityRequestType", "nsrm");
            SOAPBodyElement bodyElement = body.addBodyElement(bodyName);
            QName name = new QName("http://xmlns.oracle.com/communications/inventory/webservice/nsrm", "octet", "nsrm");
            SOAPElement element = bodyElement.addChildElement(name);


            // Add the binary attachment to the SOAP-message
            AttachmentPart attachment = request.createAttachmentPart();
            URL url = getClass().getClassLoader().getResource("./iccid.xls");
            Path p = Paths.get(url.toURI());
            byte[] fileData;
            fileData = Files.readAllBytes(p);
            attachment.setContent(fileData, "application/octet-stream; name=iccid.xls");
            attachment.setContentId("iccid.xls");
            request.addAttachmentPart(attachment);


            System.out.println("\nSoap request:\n");
            request.writeTo(System.out);


            // Message created, now lets send it

            SOAPConnectionFactory factory = SOAPConnectionFactory.newInstance();
            SOAPConnection con = factory.createConnection();
            String endpoint = "http://tr001situim.ddc.teliasonera.net:7011/InventoryWS/InventoryWSHTTP";
            SOAPMessage response = con.call(request, endpoint);
            System.out.println("\nSoap response:\n");
            response.writeTo(System.out);
            if (response.getSOAPBody().hasFault()) {
                System.out.println("\n-------------------------------\n" +
                        "There was a fault message in the response: \n" +
                        response.getSOAPBody().getFault().getFaultString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}