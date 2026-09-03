package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

public class CantWakeUp extends Card {

    public CantWakeUp() {
        addEffect(EffectSlot.SPELL, new MillEffect(4, MillRecipient.TARGET_PLAYER));
    }
}
