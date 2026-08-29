package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SetTargetPermanentSupertypeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

/**
 * Origin of Spider-Man — {1}{W} Enchantment — Saga.
 */
@CardRegistration(set = "SPM", collectorNumber = "9")
@CardRegistration(set = "SPM", collectorNumber = "218")
public class OriginOfSpiderMan extends Card {

    public OriginOfSpiderMan() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new CreateTokenEffect(
                1, "Spider", 2, 1, CardColor.GREEN,
                List.of(CardSubtype.SPIDER), Set.of(Keyword.REACH), Set.of()));

        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));
        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new SetTargetPermanentSupertypeEffect(CardSupertype.LEGENDARY, true));
        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new GrantSubtypeToTargetCreatureEffect(CardSubtype.SPIDER));
        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new GrantSubtypeToTargetCreatureEffect(CardSubtype.HERO));
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_II, Set.of(TargetFilters.creatureYouControl()));

        addEffect(EffectSlot.SAGA_CHAPTER_III,
                new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.TARGET));
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_III, Set.of(TargetFilters.creatureYouControl()));
    }
}
