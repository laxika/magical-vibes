package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.ChosenPermanentPower;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TapCreatureCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EOE", collectorNumber = "81")
public class SynthesizerLabship extends Card {

    private static final PermanentPredicate OTHER_ARTIFACT_YOU_CONTROL = new PermanentAllOfPredicate(List.of(
            new PermanentIsArtifactPredicate(),
            new PermanentControlledBySourceControllerPredicate(),
            new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
    ));

    public SynthesizerLabship() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapCreatureCost(new PermanentIsCreaturePredicate(), true, true),
                        new PutCountersOnSelfEffect(CounterType.CHARGE, new ChosenPermanentPower())
                ),
                "Tap another untapped creature you control: Put charge counters equal to its power "
                        + "on Synthesizer Labship.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));

        target(new PermanentPredicateTargetFilter(
                OTHER_ARTIFACT_YOU_CONTROL,
                "Target must be another artifact you control"
        ), 0, 1).addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new ConditionalEffect(
                new SourceCounterThreshold(2, CounterType.CHARGE),
                new AnimatePermanentsEffect(
                        new Fixed(2), new Fixed(2), List.of(), Set.of(Keyword.FLYING), null,
                        Set.of(CardType.CREATURE), GrantScope.TARGET, EffectDuration.UNTIL_END_OF_TURN,
                        OTHER_ARTIFACT_YOU_CONTROL
                )));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(9, CounterType.CHARGE),
                new GrantKeywordEffect(Set.of(Keyword.FLYING, Keyword.VIGILANCE), GrantScope.SELF)));
    }
}
