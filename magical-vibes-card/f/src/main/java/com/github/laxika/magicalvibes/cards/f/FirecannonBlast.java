package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.condition.Raid;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;

@CardRegistration(set = "XLN", collectorNumber = "145")
public class FirecannonBlast extends Card {

    public FirecannonBlast() {
        // Firecannon Blast deals 3 damage to target creature.
        // Raid — Firecannon Blast deals 6 damage instead if you attacked this turn.
        addSpellEffect(new ConditionalReplacementEffect(new Raid(), 
                new DealDamageToTargetCreatureEffect(3),
                new DealDamageToTargetCreatureEffect(6)
        ));
    }
}
