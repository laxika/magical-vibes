package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "WOE", collectorNumber = "23")
public class ThePrincessTakesFlight extends Card {

    public ThePrincessTakesFlight() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new ExileTargetPermanentAndTrackWithSourceEffect());
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_I, Set.of(TargetFilters.creature()));

        addEffect(EffectSlot.SAGA_CHAPTER_II, new BoostTargetCreatureEffect(2, 2));
        addEffect(EffectSlot.SAGA_CHAPTER_II, new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET));
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_II, Set.of(TargetFilters.creatureYouControl()));

        addEffect(EffectSlot.SAGA_CHAPTER_III, new ReturnAllCardsExiledWithSourceEffect());
    }
}
