package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;

import java.util.Set;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "NPH", collectorNumber = "101")
public class VulshokRefugee extends Card {

    public VulshokRefugee() {
        addStaticEffect(new ProtectionFromColorsEffect(Set.of(CardColor.RED)));
    }
}
