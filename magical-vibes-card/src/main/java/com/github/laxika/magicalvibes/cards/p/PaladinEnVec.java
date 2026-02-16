package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;

import java.util.Set;

public class PaladinEnVec extends Card {

    public PaladinEnVec() {
        addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(Set.of(CardColor.BLACK, CardColor.RED)));
    }
}
