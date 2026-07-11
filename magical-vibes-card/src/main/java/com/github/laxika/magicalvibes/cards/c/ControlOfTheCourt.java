package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "PTK", collectorNumber = "105")
public class ControlOfTheCourt extends Card {

    public ControlOfTheCourt() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(4));
        addEffect(EffectSlot.SPELL, new DiscardEffect(3, DiscardRecipient.CONTROLLER, true));
    }
}
