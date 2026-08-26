package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

public class VentureDeeper extends Card {

    public VentureDeeper() {
        addEffect(EffectSlot.SPELL, new MillEffect(4, MillRecipient.TARGET_PLAYER));
    }
}
