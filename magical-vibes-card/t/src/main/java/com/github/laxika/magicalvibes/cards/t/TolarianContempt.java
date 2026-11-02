package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetSpellOrCreatureOnTopOrBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MAT", collectorNumber = "8")
@CardRegistration(set = "MAT", collectorNumber = "58")
@CardRegistration(set = "MAT", collectorNumber = "108")
@CardRegistration(set = "MAT", collectorNumber = "192")
public class TolarianContempt extends Card {

    public TolarianContempt() {
        PermanentPredicate opponentCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new PutCounterOnEachMatchingPermanentEffect(
                        CounterType.REJECTION, 1, opponentCreature, EachPermanentScope.ALL_PLAYERS));

        PermanentPredicate opponentCreatureWithRejectionCounter = new PermanentAllOfPredicate(List.of(
                opponentCreature,
                new PermanentHasCountersPredicate(CounterType.REJECTION)));
        setMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER);
        target(new PermanentPredicateTargetFilter(
                        opponentCreatureWithRejectionCounter,
                        "Target must be a creature an opponent controls with a rejection counter"), 0, 99)
                .addTrigger(Triggers.endStep(ActivePlayer.YOU),
                        new PutTargetSpellOrCreatureOnTopOrBottomOfLibraryEffect());
    }
}
