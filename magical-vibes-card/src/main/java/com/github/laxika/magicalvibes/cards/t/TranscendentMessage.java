package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "MOM", collectorNumber = "83")
public class TranscendentMessage extends Card {

    public TranscendentMessage() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(new XValue()));
    }
}
