package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.condition.EnchantedPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentCounterCountAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

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
        .addStaticEffect(new StaticBoostEffect(1, 0, Subjects.attachedCreature()))

        // At the beginning of your upkeep, put a +1/+0 counter on enchanted creature. If that creature
        // has three or more +1/+0 counters on it, it deals damage equal to its power to its controller,
        // then destroy that creature and it can't be regenerated.
        .addTrigger(Triggers.upkeep(ActivePlayer.YOU), SequenceEffect.of(
                new PutCountersEffect(Subjects.attachedCreature(), CounterType.PLUS_ONE_PLUS_ZERO, 1),
                new ConditionalEffect(
                        new EnchantedPermanentMatches(
                                new PermanentCounterCountAtLeastPredicate(CounterType.PLUS_ONE_PLUS_ZERO, 3),
                                "that creature has three or more +1/+0 counters on it"),
                        SequenceEffect.of(
                                DealDamageEffect.to(Subjects.controllerOf(Subjects.attachedCreature()),
                                        new SourcePower()).dealtBy(Subjects.attachedCreature()),
                                new DestroyTargetPermanentEffect(Subjects.attachedCreature(), true)))));
    }
}
