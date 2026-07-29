package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;

import java.util.Set;

@CardRegistration(set = "MIR", collectorNumber = "27")
public class MelesseSpirit extends Card {

    public MelesseSpirit() {
        addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(Set.of(CardColor.BLACK)));
    }
}
