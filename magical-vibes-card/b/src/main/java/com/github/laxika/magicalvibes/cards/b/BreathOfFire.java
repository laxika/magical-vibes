package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "GS1", collectorNumber = "33")
public class BreathOfFire extends Card {

    public BreathOfFire() {
        // Breath of Fire deals 2 damage to target creature.
        target(TargetFilters.creature()).addSpellEffect(new DealDamageToTargetCreatureEffect(2));
    }
}
