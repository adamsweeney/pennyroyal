package com.jibstream.pennyroyalapp;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_dashboard, container, false);

        LineChart lineChart = (LineChart)v.findViewById(R.id.lineChart);
        lineChart.setAutoScaleMinMaxEnabled(false);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getAxisLeft().setDrawGridLines(false);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.setDragEnabled(false);
        lineChart.setScaleEnabled(false);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getXAxis().setTextColor(getResources().getColor(R.color.colorPrimary));
        lineChart.getAxisLeft().setTextColor(getResources().getColor(R.color.colorPrimary));
        lineChart.getXAxis().setLabelCount(1);

        final ArrayList<String> dates = new ArrayList<>();
        dates.add("Jun 10");
        dates.add("Jun 11");
        dates.add("Jun 12");
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 15f));
        entries.add(new Entry(1, 12f));
        entries.add(new Entry(2, 22f));

        LineDataSet dataSet = new LineDataSet(entries, "Balance");
        dataSet.setCircleColor(getResources().getColor(R.color.colorAccent));
        dataSet.setColor(getResources().getColor(R.color.colorAccent));
        dataSet.setDrawCircleHole(false);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(getResources().getColor(R.color.colorPrimary));
        dataSet.setValueTextColor(getResources().getColor(R.color.colorAccent));

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        lineChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return dates.get((int) value);
            }
        });

        //lineChart.getXAxis().setAxisMaximum(29);
        //lineChart.getXAxis().setAxisMinimum(0);

        Legend legend = lineChart.getLegend();
        legend.setTextColor(getResources().getColor(R.color.colorPrimary));
        legend.setForm(Legend.LegendForm.LINE);

        lineChart.invalidate();
        return v;
    }
}
