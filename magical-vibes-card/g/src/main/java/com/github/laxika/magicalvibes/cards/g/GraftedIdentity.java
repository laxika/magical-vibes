package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MID", collectorNumber = "57")
@CardRegistration(set = "DBL", collectorNumber = "57")
public class GraftedIdentity extends Card {

    public GraftedIdentity() {
        addCastingCost(new SacrificeCreatureCost());
        target(TargetFilters.creature())
                // As an additional cost to cast this spell, sacrifice a creature.
                // You control enchanted creature.
                .addStaticEffect(new ControlEnchantedCreatureEffect())
                // Enchanted creature gets +1/+1.
                .addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.ENCHANTED_CREATURE));
    }
}
