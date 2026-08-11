package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;

import java.util.Set;

@CardRegistration(set = "USG", collectorNumber = "10")
public class DiscipleOfGrace extends Card {

    public DiscipleOfGrace() {
        addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(Set.of(CardColor.BLACK)));
        addCycling("{2}");
    }
}
