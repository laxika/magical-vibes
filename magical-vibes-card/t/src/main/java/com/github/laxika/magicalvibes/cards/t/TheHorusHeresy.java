package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesCreatureToDestroyEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "40K", collectorNumber = "126")
public class TheHorusHeresy extends Card {

    public TheHorusHeresy() {
        PermanentPredicate nonlegendaryCreatureOpponent = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY)),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
        ));

        setMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER);
        addTrigger(Triggers.chapter(1),
                GainControlOfTargetEffect.withTargetPredicate(
                        ControlDuration.WHILE_SOURCE_REMAINS, nonlegendaryCreatureOpponent));
        setSagaChapterTargetGroups(1, List.of(
                new SagaChapterTargetGroup(
                        new PermanentPredicateTargetFilter(
                                nonlegendaryCreatureOpponent,
                                "Target must be a nonlegendary creature an opponent controls"),
                        0, 99)));

        addTrigger(Triggers.chapter(2), new DrawCardEffect(new PermanentCount(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentOwnedBySourceControllerPredicate())
                )), CountScope.CONTROLLER)));

        addTrigger(Triggers.chapter(3), new EachPlayerChoosesCreatureToDestroyEffect());
    }
}
