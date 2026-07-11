package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "9ED", collectorNumber = "104")
public class ThoughtCourier extends Card {

    public ThoughtCourier() {
        addActivatedAbility(new ActivatedAbility(true, null, List.of(new DrawCardEffect(), new DiscardEffect(1, DiscardRecipient.CONTROLLER)), "{T}: Draw a card, then discard a card."));
    }
}
