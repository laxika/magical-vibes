package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "62")
public class KangTheConqueror extends Card {

    public KangTheConqueror() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{U}{U}{U}",
                List.of(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        new ControllerExtraTurnEffect(1, false, false, true)
                ),
                "Power-up — {5}{U}{U}{U}: Put a +1/+1 counter on Kang. Take an extra turn after this one. "
                        + "During that turn, power-up abilities can't be activated. Activate each power-up "
                        + "ability only once. Reduce the cost by his mana cost if he entered this turn."
        ).withPowerUp());
    }
}
