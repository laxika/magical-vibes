package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
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

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "91")
public class ThePartingOfTheWays extends Card {

    public ThePartingOfTheWays() {
        addEffect(EffectSlot.SAGA_CHAPTER_I,
                new ExileTopCardsAndSuspendNonlandsWithManaValueEffect(5));

        addEffect(EffectSlot.SAGA_CHAPTER_II, new TimeTravelEffect(2));

        var opponentArtifact = new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
        var opponentArtifactFilter = new PermanentPredicateTargetFilter(
                opponentArtifact, "Target must be an artifact an opponent controls");
        addEffect(EffectSlot.SAGA_CHAPTER_III, new DestroyEachTargetPermanentEffect(opponentArtifact));
        setSagaChapterTargetGroups(EffectSlot.SAGA_CHAPTER_III,
                List.of(new SagaChapterTargetGroup(opponentArtifactFilter, 0, 99)));
        setMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER);
    }
}
