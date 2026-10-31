package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantCastSpellsSharingColorWithMostRecentSpellEffect;

@CardRegistration(set = "INV", collectorNumber = "59")
public class ManaMaze extends Card {

    public ManaMaze() {
        addStaticEffect(new CantCastSpellsSharingColorWithMostRecentSpellEffect());
    }
}
