package com.developer.harry.ribbitt.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.developer.harry.ribbitt.utils.ParseConstant;
import com.developer.harry.ribbitt.R;
import com.parse.ParseObject;

import java.util.Date;
import java.util.List;

/**
 * Created by harry on 1/15/16.
 */
public class MessageAdapter extends ArrayAdapter<ParseObject> {

    protected Context mContext;
    protected List<ParseObject> mMessage;

    public MessageAdapter(Context context, List<ParseObject> messages) {
        super(context, R.layout.message_item,messages);
        mContext = context;
        mMessage = messages;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView==null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.message_item, null);
            holder = new ViewHolder();
            holder.iconImageView = (ImageView) convertView.findViewById(R.id.message_icon);
            holder.nameLabel = (TextView) convertView.findViewById(R.id.sendrer_label);
            holder.timeLabel =(TextView) convertView.findViewById(R.id.time_label);
            convertView.setTag(holder   );
        } else{
            holder = (ViewHolder)convertView.getTag();
        }
        ParseObject message = mMessage.get(position);
        Date createdAt = message.getCreatedAt();
        long now =new Date().getTime();
        String convertedDate = DateUtils.getRelativeTimeSpanString(createdAt.getTime(),
                now,DateUtils.SECOND_IN_MILLIS).toString();
        holder.timeLabel.setText(convertedDate);
        if(message.getString(ParseConstant.KEY_FILE_TYPE).equals(ParseConstant.TYPE_IMAGE)){
            holder.iconImageView.setImageResource(R.drawable.ic_picture);
        }else {
            holder.iconImageView.setImageResource(R.drawable.ic_video);
        }
        holder.nameLabel.setText(message.getString(ParseConstant.KEY_SENDER_NAME));



     return convertView;
    }

    private static class ViewHolder{
        ImageView iconImageView;
        TextView nameLabel;
        TextView timeLabel;
    }

    public void refill(List<ParseObject> messages){
        mMessage.clear();
        mMessage.addAll(messages);
        notifyDataSetChanged();
    }

}
