package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.condition.Metalcraft;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;

@CardRegistration(set = "SOM", collectorNumber = "91")
public class GalvanicBlast extends Card {

    public GalvanicBlast() {
        // Galvanic Blast deals 2 damage to any target.
        // Metalcraft — Galvanic Blast deals 4 damage instead if you control three or more artifacts.
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(new Metalcraft(), 
                new DealDamageToAnyTargetEffect(2),
                new DealDamageToAnyTargetEffect(4)
        ));
    }
}
