package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;

import java.util.Set;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M10", collectorNumber = "85")
@CardRegistration(set = "M11", collectorNumber = "83")
public class BlackKnight extends Card {

    public BlackKnight() {
        addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(Set.of(CardColor.WHITE)));
    }
}
