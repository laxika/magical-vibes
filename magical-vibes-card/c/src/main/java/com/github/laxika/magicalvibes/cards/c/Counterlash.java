package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterlashEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "DKA", collectorNumber = "33")
public class Counterlash extends Card {

    public Counterlash() {
        addEffect(EffectSlot.SPELL, new CounterlashEffect());
    }
}
