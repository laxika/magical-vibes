package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

public class StormReading extends Card {

    public StormReading() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(4));
        addEffect(EffectSlot.SPELL, new DiscardEffect(2, DiscardRecipient.CONTROLLER));
    }
}
