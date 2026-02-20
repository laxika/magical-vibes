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
}
