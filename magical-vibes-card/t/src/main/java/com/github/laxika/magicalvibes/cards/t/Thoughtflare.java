package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "RTR", collectorNumber = "203")
public class Thoughtflare extends Card {

    public Thoughtflare() {
        addSpellEffect(new DrawCardEffect(4));
        addSpellEffect(new DiscardEffect(2, DiscardRecipient.CONTROLLER));
    }
}
