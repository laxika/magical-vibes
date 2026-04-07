package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.KickedConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;

@CardRegistration(set = "DOM", collectorNumber = "32")
public class SergeantAtArms extends Card {

    public SergeantAtArms() {
        // Kicker {2}{W}
        addEffect(EffectSlot.STATIC, new KickerEffect("{2}{W}"));
        // When this creature enters, if it was kicked, create two 1/1 white Soldier creature tokens.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new KickedConditionalEffect(
                CreateTokenEffect.whiteSoldier(2)
        ));
    }
}
