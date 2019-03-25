package com.moremoregreen.androidwebservice;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

public class WSCelsiusToFahrenheit {
    private static final String NAMESPACE = "http://www.webserviceX.NET/";
    private static final String URL = "http://www.webserviceX.NET/ConvertTemperature.asmx";
    private static final String METHOD_NAME = "ConvertTemp";
    private static final String SOAP_ACTION = "http://www.webserviceX.NET/ConvertTemp";

    String received;

    public String tempconvert(String temp) {
        received = "出了點問題...";
        try {
            SoapObject request = new SoapObject(NAMESPACE,METHOD_NAME);
            request.addProperty("Temperature", temp); //傳入攝氏溫度
            request.addProperty("FromUnit", "degreeCelsius"); //傳入被轉換單位
            request.addProperty("ToUnit", "degreeFahrenheit"); //傳入轉換單位
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.setOutputSoapObject(request);
            envelope.dotNet = true;
            HttpTransportSE httpTransportSE = new HttpTransportSE(URL);
        }catch (Exception e){
            e.printStackTrace();
        }
        return received;
    }

}
