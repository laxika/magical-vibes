package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "FDN", collectorNumber = "48")
@CardRegistration(set = "FDN", collectorNumber = "313")
public class Refute extends Card {

    public Refute() {
        addSpellEffect(new CounterSpellEffect());
        addSpellEffect(new DrawCardEffect(1));
        addSpellEffect(new DiscardEffect(1, DiscardRecipient.CONTROLLER));
    }
}
