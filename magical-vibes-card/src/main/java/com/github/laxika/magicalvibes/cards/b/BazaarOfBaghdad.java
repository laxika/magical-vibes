package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "ME3", collectorNumber = "205")
public class BazaarOfBaghdad extends Card {

    public BazaarOfBaghdad() {
        // {T}: Draw two cards, then discard three cards.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{0}",
                List.of(new DrawCardEffect(2), new DiscardEffect(3, DiscardRecipient.CONTROLLER)),
                "{T}: Draw two cards, then discard three cards."
        ));
    }
}
