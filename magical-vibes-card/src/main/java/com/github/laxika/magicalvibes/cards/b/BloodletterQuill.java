package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PutTypedCounterOnSourceCost;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "254")
public class BloodletterQuill extends Card {

    public BloodletterQuill() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new PutTypedCounterOnSourceCost(CounterType.BLOOD),
                        new DrawCardEffect(1),
                        new LoseLifeEffect(new CountersOnSource(CounterType.BLOOD), LoseLifeRecipient.CONTROLLER)
                ),
                "{2}, {T}, Put a blood counter on this artifact: Draw a card, then you lose 1 life for each blood counter on this artifact."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}{B}",
                List.of(new RemoveCounterFromSourceCost(1, CounterType.BLOOD)),
                "{U}{B}: Remove a blood counter from this artifact."
        ));
    }
}
