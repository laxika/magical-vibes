package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;

import java.util.Set;

@CardRegistration(set = "INV", collectorNumber = "248")
public class GalinasKnight extends Card {

    public GalinasKnight() {
        addStaticEffect(new ProtectionFromColorsEffect(Set.of(CardColor.RED)));
    }
}
