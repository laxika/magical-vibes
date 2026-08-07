package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CipherEncodeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "GTC", collectorNumber = "186")
public class ParanoidDelusions extends Card {

    public ParanoidDelusions() {
        addEffect(EffectSlot.SPELL, new MillEffect(3, MillRecipient.TARGET_PLAYER));
        addEffect(EffectSlot.SPELL,
                new MayEffect(new CipherEncodeEffect(), "Encode this spell on a creature you control?"));
    }
}
