package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

public class ShivaWardenOfIce extends Card {

    public ShivaWardenOfIce() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new MakeCreatureUnblockableEffect());
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_I, Set.of(TargetFilters.creature()));

        addEffect(EffectSlot.SAGA_CHAPTER_II, new MakeCreatureUnblockableEffect());
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_II, Set.of(TargetFilters.creature()));

        addEffect(EffectSlot.SAGA_CHAPTER_III, new TapPermanentsEffect(
                TapUntapScope.ALL_PERMANENTS,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsLandPredicate(),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
                ))));
        addEffect(EffectSlot.SAGA_CHAPTER_III, new ExileSelfAndReturnTransformedEffect());
    }
}
