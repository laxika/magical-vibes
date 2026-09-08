package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

@CardRegistration(set = "PLC", collectorNumber = "116")
public class BruteForce extends Card {

    public BruteForce() {
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(3, 3));
    }
}
