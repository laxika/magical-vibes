package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;

@CardRegistration(set = "USG", collectorNumber = "162")
@CardRegistration(set = "BRB", collectorNumber = "91")
public class Unnerve extends Card {

    public Unnerve() {
        addEffect(EffectSlot.SPELL, new DiscardEffect(2, DiscardRecipient.EACH_OPPONENT));
    }
}
