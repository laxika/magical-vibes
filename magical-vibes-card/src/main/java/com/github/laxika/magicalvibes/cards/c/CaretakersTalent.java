package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "6")
public class CaretakersTalent extends Card {

    public CaretakersTalent() {
        addEffect(EffectSlot.ON_ALLY_TOKEN_ENTERS_BATTLEFIELD,
                new OncePerTurnTriggerEffect(new DrawCardEffect()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(new PutCountersOnSelfEffect(CounterType.LEVEL)),
                "Level up {W} ({W}: Put a level counter on this. Level up only as a sorcery.)",
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

        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(new PermanentIsTokenPredicate())),
                "Target must be a token you control")).addEffect(
                EffectSlot.ON_SELF_REACHES_LEVEL_TWO, new CreateTokenCopyOfTargetPermanentEffect());

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(2, CounterType.LEVEL),
                new StaticBoostEffect(
                        2,
                        2,
                        GrantScope.OWN_CREATURES,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsTokenPredicate())))));
    }
}
