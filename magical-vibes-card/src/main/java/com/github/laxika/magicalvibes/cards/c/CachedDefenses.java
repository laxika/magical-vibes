package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BolsterEffect;

@CardRegistration(set = "FRF", collectorNumber = "126")
public class CachedDefenses extends Card {

    public CachedDefenses() {
        addEffect(EffectSlot.SPELL, new BolsterEffect(3));
    }
}
