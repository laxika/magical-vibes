package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;

@CardRegistration(set = "OTJ", collectorNumber = "1")
public class AnotherRound extends Card {

    public AnotherRound() {
        addEffect(EffectSlot.SPELL, FlickerEffect.controllersChooseAnyNumberCreaturesRepeatedByX());
    }
}
