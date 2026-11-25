package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "M10", collectorNumber = "194")
public class MoldAdder extends Card {

    public MoldAdder() {
        // Whenever an opponent casts a blue or black spell, you may put a +1/+1 counter on Mold Adder.
        addTrigger(Triggers.opponentCastsSpell().withFilter(new CardAnyOfPredicate(List.of(
                        new CardColorPredicate(CardColor.BLUE),
                        new CardColorPredicate(CardColor.BLACK)))),
                new MayEffect(
                        PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE),
                        "Put a +1/+1 counter on Mold Adder?"
                ));
    }
}
