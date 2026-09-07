package com.github.laxika.magicalvibes.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Progress state for Whirlwind Denial's per-opponent payment sequence.
 */
public class WhirlwindDenialState {

    public Card sourceCard;
    public UUID sourceControllerId;
    public int amount;
    public int nextTargetIndex;
    public final List<UUID> targetIds = new ArrayList<>();
    public final List<UUID> unpaidTargetIds = new ArrayList<>();

    public boolean active() {
        return sourceCard != null;
    }

    public void clear() {
        sourceCard = null;
        sourceControllerId = null;
        amount = 0;
        nextTargetIndex = 0;
        targetIds.clear();
        unpaidTargetIds.clear();
    }

    public void copyFrom(WhirlwindDenialState source) {
        sourceCard = source.sourceCard;
        sourceControllerId = source.sourceControllerId;
        amount = source.amount;
        nextTargetIndex = source.nextTargetIndex;
        targetIds.clear();
        targetIds.addAll(source.targetIds);
        unpaidTargetIds.clear();
        unpaidTargetIds.addAll(source.unpaidTargetIds);
    }
}
