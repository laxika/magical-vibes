package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;

import java.util.Set;

@CardRegistration(set = "ODY", collectorNumber = "111")
public class TreetopSentinel extends Card {

    public TreetopSentinel() {
        addStaticEffect(new ProtectionFromColorsEffect(Set.of(CardColor.GREEN)));
    }
}
