package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

import java.util.List;

@CardRegistration(set = "ROE", collectorNumber = "89")
public class SphinxOfMagosi extends Card {

    public SphinxOfMagosi() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(new DrawCardEffect(1), new PutCountersOnSourceEffect(1, 1, 1)),
                "{2}{U}: Draw a card, then put a +1/+1 counter on this creature."
        ));
    }
}
