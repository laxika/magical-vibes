package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "ISD", collectorNumber = "20")
public class MausoleumGuard extends Card {

    public MausoleumGuard() {
        // When Mausoleum Guard dies, create two 1/1 white Spirit creature tokens with flying.
        addEffect(EffectSlot.ON_DEATH, CreateTokenEffect.whiteSpirit(2));
    }
}
