package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "48")
public class BoldBiochemist extends Card {

    public BoldBiochemist() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{U}",
                List.of(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        new DrawCardEffect(2)
                ),
                "Power-up — {5}{U}: Put a +1/+1 counter on this creature and draw two cards. Activate each power-up "
                        + "ability only once. Reduce the cost by its mana cost if it entered this turn."
        ).withPowerUp());
    }
}
