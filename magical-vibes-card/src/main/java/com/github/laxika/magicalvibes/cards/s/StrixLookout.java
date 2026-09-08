package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "FDN", collectorNumber = "52")
public class StrixLookout extends Card {

    public StrixLookout() {
        // {1}{U}, {T}: Draw a card, then discard a card.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{U}",
                List.of(new DrawCardEffect(1), new DiscardEffect(1, DiscardRecipient.CONTROLLER)),
                "{1}{U}, {T}: Draw a card, then discard a card."
        ));
    }
}
