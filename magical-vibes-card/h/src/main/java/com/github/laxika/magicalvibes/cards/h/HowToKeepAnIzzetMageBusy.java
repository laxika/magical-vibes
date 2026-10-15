package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

@CardRegistration(set = "MB1", collectorNumber = "93")
public class HowToKeepAnIzzetMageBusy extends Card {

    public HowToKeepAnIzzetMageBusy() {
        addEffect(EffectSlot.SPELL, ReturnToHandEffect.selfSpell());
    }
}
