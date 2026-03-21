package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasGreatestPowerAmongControlledCreaturesPredicate;

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
        addEffect(EffectSlot.SAGA_CHAPTER_I, new PutPlusOnePlusOneCounterOnTargetCreatureEffect(1, GREATEST_POWER));

        // Chapter II: Put a +1/+1 counter on target creature you control with the greatest power
        addEffect(EffectSlot.SAGA_CHAPTER_II, new PutPlusOnePlusOneCounterOnTargetCreatureEffect(1, GREATEST_POWER));

        // Chapter III: Target creature you control with the greatest power gains
        // flying, first strike, and lifelink until end of turn
        addEffect(EffectSlot.SAGA_CHAPTER_III, new GrantKeywordEffect(
                Set.of(Keyword.FLYING, Keyword.FIRST_STRIKE, Keyword.LIFELINK),
                GrantScope.TARGET,
                GREATEST_POWER
        ));
    }
}
