package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnTargetForEachLeavingSourceCountersEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringArtifactControllerConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TMT", collectorNumber = "36")
@CardRegistration(set = "TMT", collectorNumber = "216")
@CardRegistration(set = "TMT", collectorNumber = "302")
public class DonatelloMutantMechanic extends Card {

    public DonatelloMutantMechanic() {
        var artifactYouControl = new ControlledPermanentPredicateTargetFilter(
                new PermanentIsArtifactPredicate(), "Target must be an artifact you control");
        var artifactOrCreature = new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsCreaturePredicate()));
        var artifactOrCreatureYouControl = new PermanentAllOfPredicate(List.of(
                artifactOrCreature,
                new PermanentControlledBySourceControllerPredicate()));
        var artifactOrCreatureYouControlTarget = new ControlledPermanentPredicateTargetFilter(
                artifactOrCreature, "Target must be an artifact or creature you control");

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 3),
                        new ConditionalEffect(
                                new NotCondition(new TargetPermanentMatches(new PermanentIsCreaturePredicate())),
                                new AnimatePermanentsEffect(
                                        0,
                                        0,
                                        List.of(CardSubtype.ROBOT),
                                        Set.of(),
                                        null,
                                        Set.of(CardType.CREATURE),
                                        GrantScope.TARGET,
                                        EffectDuration.PERMANENT))),
                "{T}: Put three +1/+1 counters on target artifact you control. If it isn't a creature, it becomes a 0/0 Robot creature in addition to its other types. Activate only as a sorcery.",
                artifactYouControl,
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED));

        target(artifactOrCreatureYouControlTarget, 0, 1).addEffect(
                EffectSlot.ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                new TriggeringArtifactControllerConditionalEffect(
                        new PutCountersOnTargetForEachLeavingSourceCountersEffect(artifactOrCreatureYouControl)));
    }
}
