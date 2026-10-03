package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "EOC", collectorNumber = "6")
@CardRegistration(set = "EOC", collectorNumber = "26")
public class InsightEngine extends Card {

    public InsightEngine() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new PutCountersOnSelfEffect(CounterType.CHARGE),
                        new DrawCardEffect(new CountersOnSource(CounterType.CHARGE))
                ),
                "{2}, {T}: Put a charge counter on this artifact, then draw a card for each charge counter on it."
        ));
    }
}
