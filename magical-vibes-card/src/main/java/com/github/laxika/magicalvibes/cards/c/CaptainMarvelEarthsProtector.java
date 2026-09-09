package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "11")
public class CaptainMarvelEarthsProtector extends Card {

    public CaptainMarvelEarthsProtector() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{W}{W}",
                List.of(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        new PutCountersOnSelfEffect(CounterType.INDESTRUCTIBLE)
                ),
                "Power-up — {5}{W}{W}: Put a +1/+1 counter and an indestructible counter on Captain Marvel. "
                        + "Activate each power-up ability only once. Reduce the cost by her mana cost if she entered this turn."
        ).withPowerUp());
    }
}
