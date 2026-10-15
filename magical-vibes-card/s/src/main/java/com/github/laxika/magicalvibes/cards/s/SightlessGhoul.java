package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;

@CardRegistration(set = "DKA", collectorNumber = "73")
public class SightlessGhoul extends Card {

    public SightlessGhoul() {
        addEffect(EffectSlot.STATIC, new CantBlockEffect());
    }
}
