package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;

@CardRegistration(set = "XLN", collectorNumber = "141")
public class DualShot extends Card {

    public DualShot() {
        // Dual Shot deals 1 damage to each of up to two target creatures.
        target(0, 2).addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(1));
    }
}
