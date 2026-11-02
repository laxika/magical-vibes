package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
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
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSH", collectorNumber = "197")
public class WorldWarHulk extends Card {

    public WorldWarHulk() {
        addTrigger(Triggers.chapter(1), new GrantFreeCastForNextMatchingSpellEffect(
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardAnyOfPredicate(List.of(
                                new CardColorPredicate(CardColor.RED),
                                new CardColorPredicate(CardColor.GREEN)))))));

        addTrigger(Triggers.chapter(2),
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 3));
        setSagaChapterTargetFilter(2, Set.of(TargetFilters.creatureYouControl()));

        addTrigger(Triggers.chapter(3), new DoubleTargetCreaturePowerToughnessEffect());
        addTrigger(Triggers.chapter(3), new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET));
        setSagaChapterTargetFilter(3, Set.of(TargetFilters.creatureYouControl()));
    }
}
