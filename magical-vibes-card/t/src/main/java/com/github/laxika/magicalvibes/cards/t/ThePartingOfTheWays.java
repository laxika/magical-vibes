package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsAndSuspendNonlandsWithManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.TimeTravelEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "91")
@CardRegistration(set = "WHO", collectorNumber = "696")
public class ThePartingOfTheWays extends Card {

    public ThePartingOfTheWays() {
        addTrigger(Triggers.chapter(1),
                new ExileTopCardsAndSuspendNonlandsWithManaValueEffect(5));

        addTrigger(Triggers.chapter(2), new TimeTravelEffect(2));

        var opponentArtifact = new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
        var opponentArtifactFilter = new PermanentPredicateTargetFilter(
                opponentArtifact, "Target must be an artifact an opponent controls");
        addTrigger(Triggers.chapter(3), new DestroyEachTargetPermanentEffect(opponentArtifact));
        setSagaChapterTargetGroups(3,
                List.of(new SagaChapterTargetGroup(opponentArtifactFilter, 0, 99)));
        setMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER);
    }
}
