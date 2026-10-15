package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.WaterbendCost;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "52")
public class GeyserLeaper extends Card {

    public GeyserLeaper() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new WaterbendCost(4),
                        new DrawCardEffect(1),
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER)
                ),
                "Waterbend {4}: Draw a card, then discard a card."
        ));
    }
}
