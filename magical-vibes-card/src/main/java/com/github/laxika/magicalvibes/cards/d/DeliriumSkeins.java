package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;

@CardRegistration(set = "DIS", collectorNumber = "41")
public class DeliriumSkeins extends Card {

    public DeliriumSkeins() {
        addEffect(EffectSlot.SPELL, new DiscardEffect(3, DiscardRecipient.EACH_PLAYER));
    }
}
