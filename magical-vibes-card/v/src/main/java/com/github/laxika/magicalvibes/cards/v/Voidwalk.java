package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CipherEncodeEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "GTC", collectorNumber = "55")
public class Voidwalk extends Card {

    public Voidwalk() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, FlickerEffect.exileTargetReturnAtEndStep())
                .addEffect(EffectSlot.SPELL,
                        new MayEffect(new CipherEncodeEffect(), "Encode this spell on a creature you control?"));
    }
}
