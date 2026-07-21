package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

import java.util.List;

@CardRegistration(set = "ARB", collectorNumber = "128")
public class JenaraAsuraOfWar extends Card {

    public JenaraAsuraOfWar() {
        // {1}{W}: Put a +1/+1 counter on Jenara, Asura of War.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(new PutCountersOnSourceEffect(1, 1, 1)),
                "{1}{W}: Put a +1/+1 counter on Jenara, Asura of War."
        ));
    }
}
