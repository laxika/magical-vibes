package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandCost;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "61")
public class Attunement extends Card {

    public Attunement() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new ReturnSelfToHandCost(),
                        new DrawCardEffect(3),
                        new DiscardEffect(4, DiscardRecipient.CONTROLLER)
                ),
                "Return this enchantment to its owner's hand: Draw three cards, then discard four cards."
        ));
    }
}
