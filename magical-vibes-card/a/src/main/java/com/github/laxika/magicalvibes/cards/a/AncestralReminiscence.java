package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "LCI", collectorNumber = "45")
public class AncestralReminiscence extends Card {

    public AncestralReminiscence() {
        addSpellEffect(new DrawCardEffect(3));
        addSpellEffect(new DiscardEffect(1, DiscardRecipient.CONTROLLER));
    }
}
