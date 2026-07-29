package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "285")
public class UnfulfilledDesires extends Card {

    public UnfulfilledDesires() {
        // {1}, Pay 1 life: Draw a card, then discard a card.
        addActivatedAbility(new ActivatedAbility(false, "{1}",
                List.of(new PayLifeCost(1), new DrawCardEffect(), new DiscardEffect(1, DiscardRecipient.CONTROLLER)),
                "{1}, Pay 1 life: Draw a card, then discard a card."));
    }
}
