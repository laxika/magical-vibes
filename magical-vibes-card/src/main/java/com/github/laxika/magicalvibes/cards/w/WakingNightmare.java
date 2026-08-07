package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "CHK", collectorNumber = "149")
public class WakingNightmare extends Card {

    public WakingNightmare() {
        addEffect(EffectSlot.SPELL, new DiscardEffect(2, DiscardRecipient.TARGET_PLAYER));
    }
}
