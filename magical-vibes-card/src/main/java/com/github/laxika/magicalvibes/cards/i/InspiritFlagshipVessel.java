package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.ChosenPermanentPower;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneForTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TapCreatureCost;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EOC", collectorNumber = "2")
public class InspiritFlagshipVessel extends Card {

    public InspiritFlagshipVessel() {
        PermanentPredicate anotherArtifact = new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())));
        TargetFilter artifactYouControl = new ControlledPermanentPredicateTargetFilter(
                anotherArtifact, "Target must be another artifact you control");

        target(artifactYouControl, 0, 1)
                .addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                        new ChooseOneForTargetPermanentEffect(List.of(
                                new ChooseOneEffect.ChooseOneOption(
                                        "Put a +1/+1 counter on it",
                                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE),
                                        artifactYouControl),
                                new ChooseOneEffect.ChooseOneOption(
                                        "Put two charge counters on it",
                                        new PutCounterOnTargetPermanentEffect(CounterType.CHARGE, 2),
                                        artifactYouControl)
                        )));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapCreatureCost(new PermanentIsCreaturePredicate(), true, true, false, true),
                        new PutCountersOnSelfEffect(CounterType.CHARGE, new ChosenPermanentPower())
                ),
                "Tap another creature you control: Put charge counters equal to its power on Inspirit, Flagship Vessel.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));

        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Set.of(Keyword.HEXPROOF, Keyword.INDESTRUCTIBLE),
                GrantScope.OWN_PERMANENTS,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())))));
    }
}
