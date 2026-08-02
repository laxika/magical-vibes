package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnMultiplePermanentsToHandCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "86")
public class SoratamiCloudskater extends Card {

    public SoratamiCloudskater() {
        // {2}, Return a land you control to its owner's hand: Draw a card, then discard a card.
        addActivatedAbility(new ActivatedAbility(false, "{2}",
                List.of(new ReturnMultiplePermanentsToHandCost(1, new PermanentIsLandPredicate()),
                        new DrawCardEffect(1),
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER)),
                "{2}, Return a land you control to its owner's hand: Draw a card, then discard a card."));
    }
}
