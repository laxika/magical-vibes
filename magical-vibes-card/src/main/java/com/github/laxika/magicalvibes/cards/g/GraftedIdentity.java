package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MID", collectorNumber = "57")
public class GraftedIdentity extends Card {

    public GraftedIdentity() {
        target(TargetFilters.creature())
                // As an additional cost to cast this spell, sacrifice a creature.
                .addEffect(EffectSlot.SPELL, new SacrificeCreatureCost())
                // You control enchanted creature.
                .addEffect(EffectSlot.STATIC, new ControlEnchantedCreatureEffect())
                // Enchanted creature gets +1/+1.
                .addEffect(EffectSlot.STATIC,
                        new StaticBoostEffect(1, 1, GrantScope.ENCHANTED_CREATURE));
    }
}
