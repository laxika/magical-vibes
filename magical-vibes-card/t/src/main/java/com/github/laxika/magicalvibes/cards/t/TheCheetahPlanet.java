package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToAllCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MustBlockSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByDefendingPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "574")
public class TheCheetahPlanet extends Card {

    public TheCheetahPlanet() {
        PermanentPredicate nonCatCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.CAT)))));

        target(new ControlledPermanentPredicateTargetFilter(
                nonCatCreature, "Target must be a non-Cat creature you control"))
                .addEffect(EffectSlot.PLANESWALK_TO_TRIGGERED, SequenceEffect.of(
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 2),
                        new GrantSubtypeToTargetCreatureEffect(CardSubtype.CAT)))
                .addEffect(EffectSlot.UPKEEP_TRIGGERED, SequenceEffect.of(
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 2),
                        new GrantSubtypeToTargetCreatureEffect(CardSubtype.CAT)));

        addEffect(EffectSlot.CHAOS_TRIGGERED,
                new GrantStaticEffectToAllCreaturesUntilEndOfTurnEffect(
                        new GrantTriggeredAbilityEffect(
                                EffectSlot.ON_ATTACK,
                                new MayEffect(
                                        SequenceEffect.of(
                                                new UntapPermanentsEffect(TapUntapScope.TARGET,
                                                        new PermanentControlledByDefendingPlayerPredicate()),
                                                new MustBlockSourceEffect(
                                                        null,
                                                        new PermanentControlledByDefendingPlayerPredicate())),
                                        "Have target creature defending player controls untap and block this Cat if able?"),
                                GrantScope.SELF),
                        new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.CAT))));
    }
}
