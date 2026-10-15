package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfAllPermanentsMatchingEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromPermanentsWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "199")
@CardRegistration(set = "MH2", collectorNumber = "305")
public class GeyadroneDihada extends Card {

    public GeyadroneDihada() {
        addEffect(EffectSlot.STATIC,
                new ProtectionFromPermanentsWithCountersEffect(CounterType.CORRUPTION));

        PermanentPredicate creatureOrPlaneswalker = new PermanentAnyOfPredicate(List.of(
                new PermanentIsCreaturePredicate(), new PermanentIsPlaneswalkerPredicate()));
        TargetFilter creatureOrPlaneswalkerTarget = new PermanentPredicateTargetFilter(
                creatureOrPlaneswalker, "Target must be a creature or planeswalker");
        PermanentPredicate otherCreatureOrPlaneswalker = new PermanentAllOfPredicate(List.of(
                creatureOrPlaneswalker, new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));
        TargetFilter otherCreatureOrPlaneswalkerTarget = new PermanentPredicateTargetFilter(
                otherCreatureOrPlaneswalker, "Target must be another creature or planeswalker");

        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(
                        new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT),
                        new GainLifeEffect(2),
                        new PutCounterOnTargetPermanentEffect(CounterType.CORRUPTION)
                ),
                "+1: Each opponent loses 2 life and you gain 2 life. Put a corruption counter on up to one "
                        + "other target creature or planeswalker.",
                otherCreatureOrPlaneswalkerTarget, +1, null, null, List.of(), 0, 1
        ));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(
                        new GainControlOfTargetEffect(ControlDuration.END_OF_TURN),
                        new UntapPermanentsEffect(TapUntapScope.TARGET),
                        new PutCounterOnTargetPermanentEffect(CounterType.CORRUPTION),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET)
                ),
                "-3: Gain control of target creature or planeswalker until end of turn. Untap it and put a "
                        + "corruption counter on it. It gains haste until end of turn.",
                creatureOrPlaneswalkerTarget
        ));

        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new GainControlOfAllPermanentsMatchingEffect(
                        new PermanentHasCountersPredicate(CounterType.CORRUPTION))),
                "-7: Gain control of each permanent with a corruption counter on it."
        ));
    }
}
