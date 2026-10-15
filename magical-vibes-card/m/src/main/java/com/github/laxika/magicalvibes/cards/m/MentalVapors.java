package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CipherEncodeEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "GTC", collectorNumber = "72")
public class MentalVapors extends Card {

    public MentalVapors() {
        addEffect(EffectSlot.SPELL, new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER));
        addEffect(EffectSlot.SPELL,
                new MayEffect(new CipherEncodeEffect(), "Encode this spell on a creature you control?"));
    }
}
