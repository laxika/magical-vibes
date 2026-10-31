package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "SOS", collectorNumber = "219")
public class RapturousMoment extends Card {

    public RapturousMoment() {
        // Draw three cards, then discard two cards. Add {U}{U}{R}{R}{R}.
        addSpellEffect(new DrawCardEffect(3));
        addSpellEffect(new DiscardEffect(2, DiscardRecipient.CONTROLLER));
        addSpellEffect(new AwardManaEffect(ManaColor.BLUE, 2));
        addSpellEffect(new AwardManaEffect(ManaColor.RED, 3));
    }
}
