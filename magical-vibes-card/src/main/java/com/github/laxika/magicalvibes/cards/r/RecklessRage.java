package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RIX", collectorNumber = "110")
public class RecklessRage extends Card {

    public RecklessRage() {
        // Reckless Rage deals 4 damage to target creature you don't control...
        DealDamageToTargetCreatureEffect opposingDamage = new DealDamageToTargetCreatureEffect(4);
        target(TargetFilters.creatureAnOpponentControls()).bindEffect(opposingDamage);

        // ...and 2 damage to target creature you control.
        DealDamageToTargetCreatureEffect ownDamage = new DealDamageToTargetCreatureEffect(2);
        target(TargetFilters.creatureYouControl()).bindEffect(ownDamage);
        addSimultaneousEffects(EffectSlot.SPELL, opposingDamage, ownDamage);
    }
}
