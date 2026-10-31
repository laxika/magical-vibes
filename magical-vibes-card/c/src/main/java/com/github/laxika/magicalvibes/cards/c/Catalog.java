package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "8ED", collectorNumber = "65")
@CardRegistration(set = "USG", collectorNumber = "64")
@CardRegistration(set = "SOI", collectorNumber = "51")
public class Catalog extends Card {

    public Catalog() {
        // Draw two cards, then discard a card.
        addSpellEffect(new DrawCardEffect(2));
        addSpellEffect(new DiscardEffect(1, DiscardRecipient.CONTROLLER));
    }
}
