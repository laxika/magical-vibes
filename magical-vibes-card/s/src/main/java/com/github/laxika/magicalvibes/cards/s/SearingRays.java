package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachPlayerEqualToChosenColorCreatureCountEffect;

@CardRegistration(set = "INV", collectorNumber = "165")
public class SearingRays extends Card {

    public SearingRays() {
        addEffect(EffectSlot.SPELL, new DealDamageToEachPlayerEqualToChosenColorCreatureCountEffect());
    }
}
