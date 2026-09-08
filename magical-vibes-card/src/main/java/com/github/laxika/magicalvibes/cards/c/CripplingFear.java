package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesExceptChosenTypeEffect;

@CardRegistration(set = "KHM", collectorNumber = "82")
public class CripplingFear extends Card {

    public CripplingFear() {
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesExceptChosenTypeEffect(-3, -3));
    }
}
