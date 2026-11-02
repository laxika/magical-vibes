package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "HOB", collectorNumber = "54")
public class RollRollRollRoll extends Card {

    public RollRollRollRoll() {
        for (int chapter : List.of(1, 2, 3, 4)) {
            addTrigger(Triggers.chapter(chapter), FlickerEffect.exileTargetReturnAtEndStep());
            setSagaChapterTargetGroups(chapter, List.of(new SagaChapterTargetGroup(
                    new ControlledPermanentPredicateTargetFilter(
                            new PermanentAnyOfPredicate(List.of(
                                    new PermanentIsCreaturePredicate(),
                                    new PermanentIsLandPredicate())),
                            "Target must be a creature or land you control"),
                    0,
                    1)));
        }
    }
}
