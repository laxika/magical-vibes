package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "9ED", collectorNumber = "245")
public class GreaterGood extends Card {

    public GreaterGood() {
        // Sacrifice a creature: Draw cards equal to the sacrificed creature's power, then discard three cards.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeCreatureCost(false, true),
                        new DrawCardEffect(new XValue()),
                        new DiscardEffect(3, DiscardRecipient.CONTROLLER)
                ),
                "Sacrifice a creature: Draw cards equal to the sacrificed creature's power, then discard three cards."
        ));
    }
}
