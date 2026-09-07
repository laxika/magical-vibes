package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.RequirePaymentToAttackEffect;

import java.util.Set;

@CardRegistration(set = "ELD", collectorNumber = "3")
public class ArchonOfAbsolution extends Card {

    public ArchonOfAbsolution() {
        addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(Set.of(CardColor.WHITE)));
        addEffect(EffectSlot.STATIC, new RequirePaymentToAttackEffect(1));
    }
}
