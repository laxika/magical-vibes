package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ODY", collectorNumber = "224")
public class ThermalBlast extends Card {

    public ThermalBlast() {
        // Thermal Blast deals 3 damage to target creature.
        // Threshold - Thermal Blast deals 5 damage instead if there are seven or more cards in your graveyard.
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new GraveyardCardThreshold(7, null),
                new DealDamageToTargetCreatureEffect(3),
                new DealDamageToTargetCreatureEffect(5)
        ));
    }
}
