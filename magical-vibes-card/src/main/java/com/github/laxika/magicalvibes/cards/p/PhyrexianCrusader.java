package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;

import java.util.Set;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "MBS", collectorNumber = "50")
public class PhyrexianCrusader extends Card {

    public PhyrexianCrusader() {
        addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(Set.of(CardColor.RED, CardColor.WHITE)));
    }
}
