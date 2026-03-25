package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardsEqualToControlledCreatureCountEffect;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToCardsInHandEffect;

@CardRegistration(set = "XLN", collectorNumber = "230")
public class TishanaVoiceOfThunder extends Card {

    public TishanaVoiceOfThunder() {
        addEffect(EffectSlot.STATIC, new PowerToughnessEqualToCardsInHandEffect());
        addEffect(EffectSlot.STATIC, new NoMaximumHandSizeEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardsEqualToControlledCreatureCountEffect());
    }
}
