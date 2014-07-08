package com.example.androidconparis;

import java.util.ArrayList;
import java.util.List;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * @author sylvek
 * 
 */
public class ScanListFragment extends ListFragment {

    private ArrayAdapter<String> mAdapter;

    private final List<String> macAddress = new ArrayList<String>();

    private ScanListPresenter presenter;

    private boolean isScan = false;

    public static ScanListFragment instance(ScanListPresenter presenter)
    {
        ScanListFragment instance = new ScanListFragment();
        instance.presenter = presenter;
        return instance;
    }

    private void clear()
    {
        this.mAdapter.clear();
        this.macAddress.clear();
    }

    public void scanChangeStatus(boolean enabled)
    {
        this.isScan = enabled;

        if (!this.isScan) {
            this.clear();
        }
    }

    public void addScannedDevice(final String address, final String name)
    {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run()
            {
                if (!macAddress.contains(address)) {
                    macAddress.add(address);
                    mAdapter.add((name != null) ? name : address);
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu)
    {
        // R.id.action_scan
        menu.getItem(0).setTitle((this.isScan) ? R.string.action_scan_stop : R.string.action_scan_start);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.action_scan) {
            if (this.isScan) {
                this.presenter.onScanStop();
                this.isScan = false;
            } else {
                this.presenter.onScanStart();
                this.isScan = true;
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        this.mAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
        this.setListAdapter(this.mAdapter);
        this.setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        final ListView list = getListView();
        list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                final String address = macAddress.get(position);
                presenter.onDisplayDevice(address);
            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();
        this.scanChangeStatus(false);
    }

    static interface ScanListPresenter {

        void onScanStart();

        void onScanStop();

        /**
         * @param address
         */
        void onDisplayDevice(String address);

    }
}
