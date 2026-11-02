package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SetTargetPermanentSupertypeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

/**
 * Origin of Spider-Man — {1}{W} Enchantment — Saga.
 */
@CardRegistration(set = "SPM", collectorNumber = "9")
@CardRegistration(set = "SPM", collectorNumber = "218")
@CardRegistration(set = "OM1", collectorNumber = "14")
public class OriginOfSpiderMan extends Card {

    public OriginOfSpiderMan() {
        addTrigger(Triggers.chapter(1), new CreateTokenEffect(
                1, "Spider", 2, 1, CardColor.GREEN,
                List.of(CardSubtype.SPIDER), Set.of(Keyword.REACH), Set.of()));

        addTrigger(Triggers.chapter(2),
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));
        addTrigger(Triggers.chapter(2),
                new SetTargetPermanentSupertypeEffect(CardSupertype.LEGENDARY, true));
        addTrigger(Triggers.chapter(2),
                new GrantSubtypeToTargetCreatureEffect(CardSubtype.SPIDER));
        addTrigger(Triggers.chapter(2),
                new GrantSubtypeToTargetCreatureEffect(CardSubtype.HERO));
        setSagaChapterTargetFilter(2, Set.of(TargetFilters.creatureYouControl()));
        setSagaChapterTargetRequired(2);

        addTrigger(Triggers.chapter(3),
                new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.TARGET));
        setSagaChapterTargetFilter(3, Set.of(TargetFilters.creatureYouControl()));
        setSagaChapterTargetRequired(3);
    }
}
