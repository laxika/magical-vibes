package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "SPM", collectorNumber = "86")
@CardRegistration(set = "OM1", collectorNumber = "93")
@CardRegistration(set = "MSC", collectorNumber = "808")
public class RomanticRendezvous extends Card {

    public RomanticRendezvous() {
        addSpellEffect(new DiscardEffect(1, DiscardRecipient.CONTROLLER));
        addSpellEffect(new DrawCardEffect(2));
    }
}
