package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RIX", collectorNumber = "110")
public class RecklessRage extends Card {

    public RecklessRage() {
        // Reckless Rage deals 4 damage to target creature you don't control...
        DealDamageEffect opposingDamage = DealDamageEffect.toTargetCreature(4);
        target(TargetFilters.creatureAnOpponentControls()).bindEffect(opposingDamage);

        // ...and 2 damage to target creature you control.
        DealDamageEffect ownDamage = DealDamageEffect.toTargetCreature(2);
        target(TargetFilters.creatureYouControl()).bindEffect(ownDamage);
        addSimultaneousSpellEffects(opposingDamage, ownDamage);
    }
}
