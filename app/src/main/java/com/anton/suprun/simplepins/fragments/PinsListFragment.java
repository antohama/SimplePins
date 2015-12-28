package com.anton.suprun.simplepins.fragments;

import android.app.ListFragment;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.anton.suprun.simplepins.R;
import com.anton.suprun.simplepins.activities.MapActivity;
import com.anton.suprun.simplepins.data.Constants;
import com.anton.suprun.simplepins.data.PinEntity;
import com.anton.suprun.simplepins.data.PinsDBHelper;
import com.google.android.gms.maps.model.Marker;

public class PinsListFragment extends ListFragment {
    private PinsListAdapter adapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adapter = new PinsListAdapter(getActivity(), PinsDBHelper.getInstance().getCursor(), true);
        setListAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pins_list, container, false);
        final Button buttonAdd = (Button) view.findViewById(R.id.button_add_pin);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Marker marker = ((MapActivity) getActivity()).getCurrentMarker();
                if (marker == null) {
                    Toast.makeText(getActivity(), getString(R.string.no_located_marker), Toast.LENGTH_SHORT).show();
                } else {
                    PinEntity pin = PinEntity.from(marker);
                    if (PinsDBHelper.getInstance().getPinsList().contains(pin)) {
                        Toast.makeText(getActivity(), getString(R.string.marker_exists), Toast.LENGTH_SHORT).show();
                    } else {
                        PinsDBHelper.getInstance().addPin(pin);
                        adapter.changeCursor(PinsDBHelper.getInstance().getCursor());
                        ((MapActivity) getActivity()).decoratePinned(marker);
                    }
                }
            }
        });
        Button buttonRemove = (Button) view.findViewById(R.id.button_remove_pin);
        buttonRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Marker marker = ((MapActivity) getActivity()).getCurrentMarker();
                if (marker == null || !PinsDBHelper.getInstance().getPinsList().contains(PinEntity.from(marker))) {
                    Toast.makeText(getActivity(), getString(R.string.no_selected_marker), Toast.LENGTH_SHORT).show();
                } else {
                    PinsDBHelper.getInstance().removePin(PinEntity.from(marker));
                    adapter.changeCursor(PinsDBHelper.getInstance().getCursor());
                    ((MapActivity) getActivity()).decorateUnpinned(marker);
                }
            }
        });
        return view;
    }

    class PinsListAdapter extends CursorAdapter {

        public PinsListAdapter(Context context, Cursor cursor, boolean autoRequery) {
            super(context, cursor, autoRequery);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return inflater.inflate(R.layout.pin_list_item, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            if (cursor != null) {
                String pinTitle = cursor.getString(cursor.getColumnIndex(Constants.FIELD_TITLE));
                ((TextView) view.findViewById(R.id.tv_pin_title)).setText(pinTitle);
            }
        }
    }
}
