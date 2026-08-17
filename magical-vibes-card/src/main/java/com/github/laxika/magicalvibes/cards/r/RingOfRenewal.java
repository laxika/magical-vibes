package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "FEM", collectorNumber = "89")
@CardRegistration(set = "FEM", collectorNumber = "174")
public class RingOfRenewal extends Card {

    public RingOfRenewal() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}",
                List.of(
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER, true),
                        new DrawCardEffect(2)
                ),
                "{5}, {T}: Discard a card at random, then draw two cards."
        ));
    }
}
