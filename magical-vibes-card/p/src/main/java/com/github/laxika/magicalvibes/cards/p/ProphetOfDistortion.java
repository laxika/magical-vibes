package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "OGW", collectorNumber = "46")
public class ProphetOfDistortion extends Card {

    public ProphetOfDistortion() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{C}",
                List.of(new DrawCardEffect(1)),
                "{3}{C}: Draw a card."
        ));
    }
}
