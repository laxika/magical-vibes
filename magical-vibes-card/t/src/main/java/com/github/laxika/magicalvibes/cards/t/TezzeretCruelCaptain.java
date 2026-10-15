package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.EmblemStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemTriggerStep;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EOE", collectorNumber = "2")
public class TezzeretCruelCaptain extends Card {

    private static final String EMBLEM_TEXT = "At the beginning of combat on your turn, put three +1/+1 counters on target artifact you control. If it's not a creature, it becomes a 0/0 Robot artifact creature.";

    public TezzeretCruelCaptain() {
        PermanentPredicate artifactCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsCreaturePredicate()));
        PermanentPredicate artifactYouControl = new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentControlledBySourceControllerPredicate()));
        PermanentPredicate noncreatureArtifact = new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentNotPredicate(new PermanentIsCreaturePredicate())));

        addEffect(EffectSlot.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD,
                new PutCountersOnSelfEffect(CounterType.LOYALTY));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new UntapPermanentsEffect(TapUntapScope.TARGET),
                        PutCounterOnTargetPermanentEffect.withResolutionCondition(
                                CounterType.PLUS_ONE_PLUS_ONE, 1, artifactCreature)),
                "0: Untap target artifact or creature. If it's an artifact creature, put a +1/+1 counter on it.",
                new PermanentPredicateTargetFilter(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsCreaturePredicate())),
                        "Target must be an artifact or creature"),
                0,
                null,
                null,
                List.of(),
                1,
                1
        ));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new SearchLibraryEffect(
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.ARTIFACT),
                                new CardMaxManaValuePredicate(1))),
                        LibrarySearchDestination.HAND)),
                "-3: Search your library for an artifact card with mana value 1 or less, reveal it, put it into your hand, then shuffle."
        ));

        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new CreateEmblemEffect(
                        List.of(new EmblemStepTriggerEffect(
                                EmblemTriggerStep.BEGINNING_OF_COMBAT,
                                List.of(
                                        PutCounterOnTargetPermanentEffect.withTargetRestriction(
                                                CounterType.PLUS_ONE_PLUS_ONE, 3, artifactYouControl),
                                        new ConditionalEffect(
                                                new TargetPermanentMatches(noncreatureArtifact),
                                                new AnimatePermanentsEffect(
                                                        0,
                                                        0,
                                                        List.of(CardSubtype.ROBOT),
                                                        Set.of(),
                                                        null,
                                                        Set.of(CardType.CREATURE),
                                                        GrantScope.TARGET,
                                                        EffectDuration.PERMANENT))),
                                EMBLEM_TEXT)),
                        EMBLEM_TEXT)),
                "-7: You get an emblem with \"" + EMBLEM_TEXT + "\"."
        ));
    }
}
