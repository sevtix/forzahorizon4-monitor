package dev.sevtix.horizonmonitor;

import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import java.util.List;

public class ValueDiffCallback extends DiffUtil.Callback {

    private List<Value> oldValueList = null;
    private List<Value> newValueList = null;

    public ValueDiffCallback(List<Value> oldValueList, List<Value> newValueList) {
        this.oldValueList = oldValueList;
        this.newValueList = newValueList;
    }

    @Override
    public int getOldListSize() {
        return oldValueList.size();
    }

    @Override
    public int getNewListSize() {
        return newValueList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldValueList.get(oldItemPosition).getValue() == newValueList.get(
                newItemPosition).getValue();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        final Value oldEmployee = oldValueList.get(oldItemPosition);
        final Value newEmployee = newValueList.get(newItemPosition);

        return oldEmployee.getValue().equals(newEmployee.getValue());
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        // Implement method if you're going to use ItemAnimator
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }

}
