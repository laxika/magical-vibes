package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseModeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.condition.SourceHasChosenMode;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByActivePlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "FRF", collectorNumber = "8")
public class CitadelSiege extends Card {

    private static final String KHANS = "Khans";
    private static final String DRAGONS = "Dragons";

    public CitadelSiege() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChooseModeOnEnterEffect(List.of(KHANS, DRAGONS)));

        var target = target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(), "Target must be a creature"));
        target.addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new ConditionalEffect(new SourceHasChosenMode(KHANS),
                        PutCounterOnTargetPermanentEffect.withTargetRestriction(
                                CounterType.PLUS_ONE_PLUS_ONE, 2,
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentControlledBySourceControllerPredicate())))));
        target.addEffect(EffectSlot.OPPONENT_BEGINNING_OF_COMBAT_TRIGGERED,
                new ConditionalEffect(new SourceHasChosenMode(DRAGONS),
                        new TapPermanentsEffect(TapUntapScope.TARGET,
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentControlledByActivePlayerPredicate())))));
    }
}
