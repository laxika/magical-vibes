package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;

import java.util.List;

@CardRegistration(set = "FUT", collectorNumber = "90")
public class StreetWraith extends Card {

    public StreetWraith() {
        addHandActivatedAbility(new ActivatedAbility(false, null,
                List.of(new PayLifeCost(2), new DrawCardEffect(1)),
                "Cycling {0}—Pay 2 life (Pay 2 life, Discard this card: Draw a card.)"));
    }
}
