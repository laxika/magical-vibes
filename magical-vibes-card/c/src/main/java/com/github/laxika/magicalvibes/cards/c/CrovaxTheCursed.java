package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "STH", collectorNumber = "55")
@CardRegistration(set = "TPR", collectorNumber = "90")
@CardRegistration(set = "VMA", collectorNumber = "110")
public class CrovaxTheCursed extends Card {

    public CrovaxTheCursed() {
        // Crovax enters with four +1/+1 counters on it.
        addStaticEffect(new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(4)));

        // At the beginning of your upkeep, you may sacrifice a creature. If you do, put a +1/+1
        // counter on Crovax. If you don't, remove a +1/+1 counter from Crovax.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                UnlessPaysEffect.youMayPay(
                                new SacrificePermanentCost(new PermanentIsCreaturePredicate(), "creature", false),
                                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE))
                        .ifNotPaid(RemoveCountersEffect.fromSelf(CounterType.PLUS_ONE_PLUS_ONE, 1)));

        // {B}: Crovax gains flying until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(new GrantKeywordEffect(Keyword.FLYING, Subjects.source())),
                "{B}: Crovax gains flying until end of turn."
        ));
    }
}
