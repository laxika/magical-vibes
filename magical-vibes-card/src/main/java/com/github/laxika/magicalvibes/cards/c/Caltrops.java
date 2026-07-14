package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;

@CardRegistration(set = "7ED", collectorNumber = "288")
public class Caltrops extends Card {

    public Caltrops() {
        // Whenever a creature attacks, this artifact deals 1 damage to it. The triggering attacker
        // is the (non-targeting) targetId, so a plain target-creature damage effect hits it.
        addEffect(EffectSlot.ON_ANY_CREATURE_ATTACKS, new DealDamageToTargetCreatureEffect(1));
    }
}
