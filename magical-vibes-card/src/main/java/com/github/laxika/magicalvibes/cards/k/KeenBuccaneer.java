package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "48")
public class KeenBuccaneer extends Card {

    public KeenBuccaneer() {
        // Exhaust — {1}{U}: Draw a card, then discard a card. Put a +1/+1 counter on this creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(
                        new DrawCardEffect(1),
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)
                ),
                "Exhaust — {1}{U}: Draw a card, then discard a card. Put a +1/+1 counter on this creature. "
                        + "(Activate each exhaust ability only once.)"
        ).withMaxActivationsPerGame(1).withExhaust());
    }
}
