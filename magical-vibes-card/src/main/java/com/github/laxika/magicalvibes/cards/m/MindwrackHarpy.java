package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "THB", collectorNumber = "276")
public class MindwrackHarpy extends Card {

    public MindwrackHarpy() {
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new MillEffect(3, MillRecipient.CONTROLLER));
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new MillEffect(3, MillRecipient.EACH_OPPONENT));
    }
}
