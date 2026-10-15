package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AddCounterToEnchantedCreatureThenDestroyAtThresholdEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "166")
public class ConsumingFerocity extends Card {

    public ConsumingFerocity() {
        // Enchant non-Wall creature
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.WALL)))),
                "Target must be a non-Wall creature"))

        // Enchanted creature gets +1/+0.
        .addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 0, GrantScope.ENCHANTED_CREATURE))

        // At the beginning of your upkeep, put a +1/+0 counter on enchanted creature. If that creature
        // has three or more +1/+0 counters on it, it deals damage equal to its power to its controller,
        // then destroy that creature and it can't be regenerated.
        .addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new AddCounterToEnchantedCreatureThenDestroyAtThresholdEffect(CounterType.PLUS_ONE_PLUS_ZERO, 3));
    }
}
