package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RIX", collectorNumber = "110")
public class RecklessRage extends Card {

    public RecklessRage() {
        // Reckless Rage deals 4 damage to target creature you don't control.
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(4));

        // Reckless Rage deals 2 damage to target creature you control.
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(2));
    }
}
