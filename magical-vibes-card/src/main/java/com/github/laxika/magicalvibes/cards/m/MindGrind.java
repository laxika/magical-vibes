package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.RevealUntilLandsMillTargetPlayerEffect;

@CardRegistration(set = "GTC", collectorNumber = "178")
public class MindGrind extends Card {

    public MindGrind() {
        // Each opponent reveals cards from the top of their library until they reveal X land cards,
        // then puts all cards revealed this way into their graveyard. The paid X rides on the stack
        // entry's xValue, which XValue reads at resolution.
        addEffect(EffectSlot.SPELL,
                new RevealUntilLandsMillTargetPlayerEffect(new XValue(), MillRecipient.EACH_OPPONENT));
    }
}
