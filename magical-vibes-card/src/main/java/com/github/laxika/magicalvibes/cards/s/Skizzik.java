package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.NotKickedConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

@CardRegistration(set = "DOM", collectorNumber = "145")
public class Skizzik extends Card {

    public Skizzik() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{R}"));
        addEffect(EffectSlot.END_STEP_TRIGGERED, new NotKickedConditionalEffect(new SacrificeSelfEffect()));
    }
}
