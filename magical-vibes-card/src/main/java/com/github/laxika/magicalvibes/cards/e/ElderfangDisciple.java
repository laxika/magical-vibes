package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;

@CardRegistration(set = "KHM", collectorNumber = "93")
public class ElderfangDisciple extends Card {

    public ElderfangDisciple() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT));
    }
}
