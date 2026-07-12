package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "SHM", collectorNumber = "29")
public class BitingTether extends Card {

    public BitingTether() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ))
                // You control enchanted creature.
                .addEffect(EffectSlot.STATIC, new ControlEnchantedCreatureEffect())

                // At the beginning of your upkeep, put a -1/-1 counter on enchanted creature.
                .addEffect(EffectSlot.UPKEEP_TRIGGERED, new PutCounterOnEnchantedCreatureEffect(CounterType.MINUS_ONE_MINUS_ONE));
    }
}
