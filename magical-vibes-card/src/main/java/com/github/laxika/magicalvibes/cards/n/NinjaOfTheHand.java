package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "108")
public class NinjaOfTheHand extends Card {

    public NinjaOfTheHand() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{B}",
                List.of(
                        new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)
                ),
                "Power-up — {4}{B}: Each opponent discards a card. Put a +1/+1 counter on this creature. "
                        + "Activate each power-up ability only once. Reduce the cost by its mana cost if it entered this turn."
        ).withPowerUp());
    }
}
