package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasGreatestPowerAmongControlledCreaturesPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

/**
 * Triumph of Gerrard — {1}{W} Enchantment — Saga
 *
 * (As this Saga enters and after your draw step, add a lore counter. Sacrifice after III.)
 * I, II — Put a +1/+1 counter on target creature you control with the greatest power.
 * III — Target creature you control with the greatest power gains flying, first strike,
 *        and lifelink until end of turn.
 */
@CardRegistration(set = "DOM", collectorNumber = "38")
public class TriumphOfGerrard extends Card {

    private static final PermanentHasGreatestPowerAmongControlledCreaturesPredicate GREATEST_POWER =
            new PermanentHasGreatestPowerAmongControlledCreaturesPredicate();

    public TriumphOfGerrard() {
        // Chapter I: Put a +1/+1 counter on target creature you control with the greatest power
        addTrigger(Triggers.chapter(1),
                PutCounterOnTargetPermanentEffect.withTargetRestriction(CounterType.PLUS_ONE_PLUS_ONE, 1, GREATEST_POWER));

        // Chapter II: Put a +1/+1 counter on target creature you control with the greatest power
        addTrigger(Triggers.chapter(2),
                PutCounterOnTargetPermanentEffect.withTargetRestriction(CounterType.PLUS_ONE_PLUS_ONE, 1, GREATEST_POWER));

        // Chapter III: Target creature you control with the greatest power gains
        // flying, first strike, and lifelink until end of turn
        addTrigger(Triggers.chapter(3), new GrantKeywordEffect(
                Set.of(Keyword.FLYING, Keyword.FIRST_STRIKE, Keyword.LIFELINK),
                GrantScope.TARGET,
                GREATEST_POWER
        ));

        setSagaChapterTargetRequired(1);
        setSagaChapterTargetRequired(2);
        setSagaChapterTargetRequired(3);
    }
}
