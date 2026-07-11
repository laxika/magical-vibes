package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "159")
@CardRegistration(set = "M10", collectorNumber = "105")
@CardRegistration(set = "M11", collectorNumber = "105")
@CardRegistration(set = "9ED", collectorNumber = "145")
@CardRegistration(set = "POR", collectorNumber = "101")
@CardRegistration(set = "P02", collectorNumber = "78")
public class MindRot extends Card {

    public MindRot() {
        addEffect(EffectSlot.SPELL, new DiscardEffect(2, DiscardRecipient.TARGET_PLAYER));
    }
}
