/**
 *
 * Copyright (C) 2012 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 * Created on 8 juil. 2014
 *
 */
package com.example.androidconparis;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * @author sylvek
 * 
 */
public class DeviceFragment extends Fragment {

    private TextView temperature, unit;

    public static DeviceFragment instance()
    {
        return new DeviceFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_device, container, false);
        this.temperature = (TextView) root.findViewById(R.id.temperature);
        this.unit = (TextView) root.findViewById(R.id.unit);
        return root;
    }

    public void setTemperature(String temperature)
    {
        this.temperature.setText(temperature);
    }

    public void setUnit(String unit)
    {
        this.unit.setText(unit);
    }
}
