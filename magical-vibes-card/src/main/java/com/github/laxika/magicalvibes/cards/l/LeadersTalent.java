package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TMT", collectorNumber = "13")
public class LeadersTalent extends Card {

    public LeadersTalent() {
        target(TargetFilters.attackingCreature()).addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}",
                List.of(new PutCountersOnSelfEffect(CounterType.LEVEL)),
                "Level up {2}{W} ({2}{W}: Put a level counter on this. Level up only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(
                new NotCondition(new SourceCounterThreshold(1, CounterType.LEVEL)),
                "This Class is already level 2 or higher."));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{W}",
                List.of(new PutCountersOnSelfEffect(CounterType.LEVEL)),
                "Level up {3}{W} ({3}{W}: Put a level counter on this. Level up only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(
                new AllOf(List.of(
                        new SourceCounterThreshold(1, CounterType.LEVEL),
                        new NotCondition(new SourceCounterThreshold(2, CounterType.LEVEL)))),
                "This Class must be level 2."));

        addEffect(EffectSlot.ON_ALLY_CREATURE_LEAVES_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasCountersPredicate(CounterType.ANY),
                        new ConditionalEffect(
                                new SourceCounterThreshold(1, CounterType.LEVEL),
                                new GainLifeEffect(2))));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, SpellCastTriggerEffect.withIntervening(
                null,
                List.of(new PutCounterOnEachControlledPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE,
                        1,
                        new PermanentIsCreaturePredicate())),
                new SourceCounterThreshold(2, CounterType.LEVEL)));
    }
}
