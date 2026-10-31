package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;

@CardRegistration(set = "UDS", collectorNumber = "39")
public class MetathranSoldier extends Card {

    public MetathranSoldier() {
        addStaticEffect(new CantBeBlockedEffect());
    }
}
