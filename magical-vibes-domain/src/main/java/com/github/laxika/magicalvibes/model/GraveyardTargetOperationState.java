package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.List;
import java.util.UUID;

public class GraveyardTargetOperationState {

    public Card card;
    public UUID controllerId;
    public List<CardEffect> effects;
    public StackEntryType entryType;
    public int xValue;
    public boolean anyNumber;
    /** Target player for effects like "Target player shuffles ... from their graveyard" */
    public UUID targetPlayerId;
    /** Whether the spell is being cast with flashback */
    public boolean flashback;
    /** Source permanent ID for saga chapter graveyard targets (used in SBA check CR 714.4). */
    public UUID sourcePermanentId;
    /** Chapter name for saga chapter graveyard targets (e.g. "I", "II"). */
    public String chapterName;
}
