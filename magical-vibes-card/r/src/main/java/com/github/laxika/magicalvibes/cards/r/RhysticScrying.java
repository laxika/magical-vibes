package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;

@CardRegistration(set = "PCY", collectorNumber = "44")
public class RhysticScrying extends Card {

    public RhysticScrying() {
        addSpellEffect(new DrawCardEffect(3));
        addSpellEffect(new MayPayManaEffect(
                "{2}",
                new DiscardEffect(3, DiscardRecipient.CONTROLLER),
                "Pay {2} to discard three cards?",
                MayPayPayer.ANY_PLAYER));
    }
}
