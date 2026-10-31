package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "ODY", collectorNumber = "70")
@CardRegistration(set = "SLD", collectorNumber = "1768")
public class CarefulStudy extends Card {

    public CarefulStudy() {
        // Draw two cards, then discard two cards.
        addSpellEffect(new DrawCardEffect(2));
        addSpellEffect(new DiscardEffect(2, DiscardRecipient.CONTROLLER));
    }
}
