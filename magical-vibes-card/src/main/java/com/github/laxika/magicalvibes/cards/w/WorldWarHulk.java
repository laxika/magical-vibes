package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DoubleTargetCreaturePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFreeCastForNextMatchingSpellEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSH", collectorNumber = "197")
public class WorldWarHulk extends Card {

    public WorldWarHulk() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new GrantFreeCastForNextMatchingSpellEffect(
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardAnyOfPredicate(List.of(
                                new CardColorPredicate(CardColor.RED),
                                new CardColorPredicate(CardColor.GREEN)))))));

        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 3));
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_II, Set.of(TargetFilters.creatureYouControl()));

        addEffect(EffectSlot.SAGA_CHAPTER_III, new DoubleTargetCreaturePowerToughnessEffect());
        addEffect(EffectSlot.SAGA_CHAPTER_III, new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET));
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_III, Set.of(TargetFilters.creatureYouControl()));
    }
}
