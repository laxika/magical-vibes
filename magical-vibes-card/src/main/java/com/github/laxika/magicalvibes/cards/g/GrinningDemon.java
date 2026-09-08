package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;

@CardRegistration(set = "ONS", collectorNumber = "153")
public class GrinningDemon extends Card {

    public GrinningDemon() {
        addMorph("{2}{B}{B}");
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new LoseLifeEffect(2));
    }
}
