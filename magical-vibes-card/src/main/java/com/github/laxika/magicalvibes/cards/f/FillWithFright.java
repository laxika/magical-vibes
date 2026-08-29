package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "5DN", collectorNumber = "50")
public class FillWithFright extends Card {

    public FillWithFright() {
        addEffect(EffectSlot.SPELL, new DiscardEffect(2, DiscardRecipient.TARGET_PLAYER));
        addEffect(EffectSlot.SPELL, new ScryEffect(2));
    }
}
