package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "STX", collectorNumber = "4")
public class IntroductionToProphecy extends Card {

    public IntroductionToProphecy() {
        addEffect(EffectSlot.SPELL, new ScryEffect(2));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
