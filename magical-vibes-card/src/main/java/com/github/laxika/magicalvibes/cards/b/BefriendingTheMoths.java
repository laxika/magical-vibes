package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "NEO", collectorNumber = "4")
public class BefriendingTheMoths extends Card {

    public BefriendingTheMoths() {
        addChapterEffects(EffectSlot.SAGA_CHAPTER_I);
        addChapterEffects(EffectSlot.SAGA_CHAPTER_II);
        addEffect(EffectSlot.SAGA_CHAPTER_III, new ExileSelfAndReturnTransformedEffect(true));
    }

    private void addChapterEffects(EffectSlot chapter) {
        addEffect(chapter, new BoostTargetCreatureEffect(1, 1));
        addEffect(chapter, new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET));
        setSagaChapterTargetFilter(chapter, Set.of(TargetFilters.creatureYouControl()));
    }
}
