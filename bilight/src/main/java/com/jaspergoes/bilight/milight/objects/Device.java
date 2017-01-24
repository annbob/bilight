package com.jaspergoes.bilight.milight.objects;

public class Device implements Comparable<Device> {

    public final String addrIP;
    public final String addrMAC;

    public Device(String addrIP, String addrMAC) {
        this.addrIP = addrIP;
        this.addrMAC = addrMAC;
    }

    @Override
    public int compareTo(Device compareTo) {
        return this.addrIP.compareTo(compareTo.addrIP);
    }

}
