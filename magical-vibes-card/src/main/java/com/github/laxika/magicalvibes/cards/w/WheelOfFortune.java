package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;

@CardRegistration(set = "SUM", collectorNumber = "185")
public class WheelOfFortune extends Card {

    public WheelOfFortune() {
        // Each player discards their hand, then draws seven cards.
        addEffect(EffectSlot.SPELL, new DiscardHandEffect(DiscardRecipient.EACH_PLAYER));
        addEffect(EffectSlot.SPELL, new EachPlayerDrawsCardEffect(7));
    }
}
