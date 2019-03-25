package com.moremoregreen.androidwebservice;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

public class WSClass {
    public static final String NAMESPACE = "http://tempuri.org/";
    private String URL = "http://120.109.34.168/ProjectWS/ProjectWS.asmx";
    private static final String METHOD_NAME = "GetData";
    private static final String SOAP_ACTION = "http://tempuri.org/GetData";
    // 回傳的資料
    String receivedString;
    public String toWebService(String sqlstr) {
        try {
            SoapObject request = new SoapObject(NAMESPACE,METHOD_NAME);
            PropertyInfo celsiusPI= new PropertyInfo();
            celsiusPI.setName("SqlStr");
            celsiusPI.setValue(sqlstr); //sql指令傳入
            celsiusPI.setType(double.class);
            request.addProperty(celsiusPI);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);
            envelope.setOutputSoapObject(request);
            envelope.dotNet = true;
            HttpTransportSE httpTransportSE = new HttpTransportSE(URL);
            httpTransportSE.call(SOAP_ACTION,envelope);
            SoapPrimitive Result = (SoapPrimitive) envelope.getResponse();
            receivedString = Result.toString();
            if(httpTransportSE !=null){
                httpTransportSE.reset();
            }
        } catch (Exception e) {
            receivedString = "not work";
            return receivedString;
        }
        return receivedString;
    }

}
