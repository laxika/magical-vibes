package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RemoveAllPoisonCountersAndDamageTargetPlayerEffect;

@CardRegistration(set = "HML", collectorNumber = "9")
public class Leeches extends Card {

    public Leeches() {
        // "Target player loses all poison counters. Leeches deals that much damage to that player."
        addEffect(EffectSlot.SPELL, new RemoveAllPoisonCountersAndDamageTargetPlayerEffect());
    }
}
