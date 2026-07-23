package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "5ED", collectorNumber = "74")
@CardRegistration(set = "ICE", collectorNumber = "60")
public class BindingGrasp extends Card {

    public BindingGrasp() {
        // Enchant creature
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ))
                // At the beginning of your upkeep, sacrifice this Aura unless you pay {1}{U}.
                .addEffect(EffectSlot.UPKEEP_TRIGGERED,
                        new ForcedCostOrElseEffect(
                                new PayManaCost("{1}{U}"),
                                List.of(new SacrificeSelfEffect()),
                                true))
                // You control enchanted creature.
                .addEffect(EffectSlot.STATIC, new ControlEnchantedCreatureEffect())
                // Enchanted creature gets +0/+1.
                .addEffect(EffectSlot.STATIC,
                        new StaticBoostEffect(0, 1, Set.of(), GrantScope.ENCHANTED_CREATURE));
    }
}
