package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReadAheadEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

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
        addStaticEffect(new ReadAheadEffect(false));

        // Chapter I: You and target opponent each draw two cards.
        addTrigger(Triggers.chapter(1), new DrawCardEffect(2), new DrawCardForTargetPlayerEffect(2, false, true));
        setSagaChapterTargetFilter(1, Set.of(
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent")));

        // Chapter II: Create a 1/1 white Bird creature token with flying.
        addTrigger(Triggers.chapter(2), new CreateTokenEffect(
                1, "Bird", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.BIRD), Set.of(Keyword.FLYING), Set.of()));

        // Chapter III: Put a +1/+1 counter on each of up to two target creatures.
        addTrigger(Triggers.chapter(3),
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));
        setSagaChapterTargetGroups(3, List.of(
                new SagaChapterTargetGroup(TargetFilters.creature(), 0, 2)));
    }
}
