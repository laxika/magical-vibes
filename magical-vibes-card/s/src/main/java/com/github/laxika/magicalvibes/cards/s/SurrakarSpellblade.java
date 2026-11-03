package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ROE", collectorNumber = "90")
@CardRegistration(set = "MM2", collectorNumber = "60")
public class SurrakarSpellblade extends Card {

    public SurrakarSpellblade() {
        addTrigger(Triggers.youCastSpell()
                        .withFilter(new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY)))),
                new MayEffect(new PutCountersOnSelfEffect(CounterType.CHARGE), "Put a charge counter on Surrakar Spellblade?"));

        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new MayEffect(
                        new DrawCardEffect(new CountersOnSource(CounterType.CHARGE)),
                        "Draw cards equal to the number of charge counters on Surrakar Spellblade?"));
    }
}
