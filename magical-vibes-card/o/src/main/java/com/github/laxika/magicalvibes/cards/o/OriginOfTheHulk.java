package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

/**
 * Origin of the Hulk — {2}{G} Enchantment — Saga.
 */
@CardRegistration(set = "MSC", collectorNumber = "728")
public class OriginOfTheHulk extends Card {

    public OriginOfTheHulk() {
        // Chapter I: Create a 1/1 green and white Citizen creature token.
        addTrigger(Triggers.chapter(1), new CreateTokenEffect(
                1, "Citizen", 1, 1, CardColor.GREEN,
                Set.of(CardColor.GREEN, CardColor.WHITE), List.of(CardSubtype.CITIZEN)));

        // Chapter II: Put two +1/+1 counters on target creature you control.
        addTrigger(Triggers.chapter(2),
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 2));
        setSagaChapterTargetFilter(2, Set.of(TargetFilters.creatureYouControl()));
        setSagaChapterTargetRequired(2);

        // Chapter III: Target creature you control gets +3/+3 and gains trample until end of turn.
        addTrigger(Triggers.chapter(3),
                new BoostTargetCreatureEffect(3, 3),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET));
        setSagaChapterTargetFilter(3, Set.of(TargetFilters.creatureYouControl()));
        setSagaChapterTargetRequired(3);
    }
}
