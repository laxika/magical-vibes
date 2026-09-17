package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

/**
 * Love Song of Night and Day - {2}{W} Enchantment - Saga
 *
 * Chapter I: You and target opponent each draw two cards.
 * Chapter II: Create a 1/1 white Bird creature token with flying.
 * Chapter III: Put a +1/+1 counter on each of up to two target creatures.
 */
@CardRegistration(set = "DMU", collectorNumber = "25")
public class LoveSongOfNightAndDay extends Card {

    public LoveSongOfNightAndDay() {
        // Chapter I: You and target opponent each draw two cards.
        addEffect(EffectSlot.SAGA_CHAPTER_I, new DrawCardEffect(2));
        addEffect(EffectSlot.SAGA_CHAPTER_I, new DrawCardForTargetPlayerEffect(2, false, true));
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_I, Set.of(
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent")));

        // Chapter II: Create a 1/1 white Bird creature token with flying.
        addEffect(EffectSlot.SAGA_CHAPTER_II, new CreateTokenEffect(
                1, "Bird", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.BIRD), Set.of(Keyword.FLYING), Set.of()));

        // Chapter III: Put a +1/+1 counter on each of up to two target creatures.
        addEffect(EffectSlot.SAGA_CHAPTER_III,
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));
        setSagaChapterTargetGroups(EffectSlot.SAGA_CHAPTER_III, List.of(
                new SagaChapterTargetGroup(TargetFilters.creature(), 0, 2)));
    }
}
