package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;

import java.util.Set;

@CardRegistration(set = "ODY", collectorNumber = "288")
public class IridescentAngel extends Card {

    public IridescentAngel() {
        addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(Set.of(
                CardColor.WHITE,
                CardColor.BLUE,
                CardColor.BLACK,
                CardColor.RED,
                CardColor.GREEN)));
    }
}
