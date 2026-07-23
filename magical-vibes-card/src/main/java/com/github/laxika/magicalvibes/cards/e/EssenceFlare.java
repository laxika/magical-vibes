package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "ICE", collectorNumber = "69")
public class EssenceFlare extends Card {

    public EssenceFlare() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ))
                // Enchanted creature gets +2/+0.
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 0, GrantScope.ENCHANTED_CREATURE))

                // At the beginning of the upkeep of enchanted creature's controller,
                // put a -0/-1 counter on that creature.
                .addEffect(EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED,
                        new PutCounterOnEnchantedCreatureEffect(CounterType.MINUS_ZERO_MINUS_ONE));
    }
}
