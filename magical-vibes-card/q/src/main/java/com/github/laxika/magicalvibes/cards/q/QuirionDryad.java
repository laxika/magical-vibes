package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "10E", collectorNumber = "287")
@CardRegistration(set = "M13", collectorNumber = "184")
@CardRegistration(set = "M21", collectorNumber = "198")
@CardRegistration(set = "PLS", collectorNumber = "89")
public class QuirionDryad extends Card {

    public QuirionDryad() {
        addTrigger(Triggers.youCastSpell().withFilter(new CardAnyOfPredicate(List.of(
                        new CardColorPredicate(CardColor.WHITE),
                        new CardColorPredicate(CardColor.BLUE),
                        new CardColorPredicate(CardColor.BLACK),
                        new CardColorPredicate(CardColor.RED)))),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
