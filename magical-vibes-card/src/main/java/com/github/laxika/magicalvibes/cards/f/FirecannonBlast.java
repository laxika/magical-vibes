package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RaidReplacementEffect;

@CardRegistration(set = "XLN", collectorNumber = "145")
public class FirecannonBlast extends Card {

    public FirecannonBlast() {
        // Firecannon Blast deals 3 damage to target creature.
        // Raid — Firecannon Blast deals 6 damage instead if you attacked this turn.
        addEffect(EffectSlot.SPELL, new RaidReplacementEffect(
                new DealDamageToTargetCreatureEffect(3),
                new DealDamageToTargetCreatureEffect(6)
        ));
    }
}
