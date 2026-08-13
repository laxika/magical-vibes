package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MaySacrificePermanentForCounterOrRemoveSourceCounterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "STH", collectorNumber = "55")
@CardRegistration(set = "TPR", collectorNumber = "90")
public class CrovaxTheCursed extends Card {

    public CrovaxTheCursed() {
        // Crovax enters with four +1/+1 counters on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(4)));

        // At the beginning of your upkeep, you may sacrifice a creature. If you do, put a +1/+1
        // counter on Crovax. If you don't, remove a +1/+1 counter from Crovax.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new MaySacrificePermanentForCounterOrRemoveSourceCounterEffect(
                        new PermanentIsCreaturePredicate(), CounterType.PLUS_ONE_PLUS_ONE, "a creature"));

        // {B}: Crovax gains flying until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)),
                "{B}: Crovax gains flying until end of turn."
        ));
    }
}
