package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;

@CardRegistration(set = "RAV", collectorNumber = "216")
public class Moroii extends Card {

    public Moroii() {
        // At the beginning of your upkeep, you lose 1 life.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new LoseLifeEffect(1));
    }
}
