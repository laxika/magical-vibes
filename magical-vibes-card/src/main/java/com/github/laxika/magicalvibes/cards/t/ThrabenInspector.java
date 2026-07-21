package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "INR", collectorNumber = "45")
public class ThrabenInspector extends Card {

    public ThrabenInspector() {
        // When this creature enters, investigate.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.ofClueToken(1));
    }
}
