package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "244")
public class CollectorsVault extends Card {

    public CollectorsVault() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new DrawCardEffect(1),
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER),
                        CreateTokenEffect.ofTreasureToken(1)
                ),
                "{2}, {T}: Draw a card, then discard a card. Create a Treasure token."
        ));
    }
}
