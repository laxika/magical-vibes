package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

public class ShivaWardenOfIce extends Card {

    public ShivaWardenOfIce() {
        addTrigger(Triggers.chapter(1), new MakeCreatureUnblockableEffect());
        setSagaChapterTargetFilter(1, Set.of(TargetFilters.creature()));

        addTrigger(Triggers.chapter(2), new MakeCreatureUnblockableEffect());
        setSagaChapterTargetFilter(2, Set.of(TargetFilters.creature()));

        addTrigger(Triggers.chapter(3), new TapPermanentsEffect(
                TapUntapScope.ALL_PERMANENTS,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsLandPredicate(),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
                ))));
        addTrigger(Triggers.chapter(3), new ExileSelfAndReturnTransformedEffect());
    }
}
