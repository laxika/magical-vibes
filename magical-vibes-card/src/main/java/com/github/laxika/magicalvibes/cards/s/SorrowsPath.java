package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SwapBlockingAssignmentsBetweenTwoCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "DRK", collectorNumber = "119")
public class SorrowsPath extends Card {

    public SorrowsPath() {
        PermanentPredicateTargetFilter blockingCreatureOpponentControls = new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsBlockingPredicate(),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))),
                "Target must be a blocking creature an opponent controls");

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SwapBlockingAssignmentsBetweenTwoCreaturesEffect()),
                "{T}: Choose two target blocking creatures controlled by the same opponent. If each of "
                        + "those creatures could block all creatures that the other is blocking, remove both "
                        + "of them from combat. Each one then blocks all creatures the other was blocking.",
                null,
                null,
                null,
                null,
                List.of(blockingCreatureOpponentControls, blockingCreatureOpponentControls),
                2,
                2
        ).withMultiTargetConstraint(MultiTargetConstraint.CONTROLLED_BY_FIRST_TARGET));

        addEffect(EffectSlot.ON_ALLY_PERMANENT_BECOMES_TAPPED, new TriggeringPermanentConditionalEffect(
                new PermanentIsSourceCardPredicate(),
                SequenceEffect.of(
                        new DealDamageToPlayersEffect(2, DamageRecipient.CONTROLLER),
                        new DealDamageToEachMatchingPermanentEffect(2,
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentControlledBySourceControllerPredicate())),
                                EachPermanentScope.ALL_PLAYERS))));
    }
}
