package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;

import java.util.Set;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "MBS", collectorNumber = "14")
@CardRegistration(set = "MM2", collectorNumber = "25")
public class MirranCrusader extends Card {

    public MirranCrusader() {
        addStaticEffect(new ProtectionFromColorsEffect(Set.of(CardColor.BLACK, CardColor.GREEN)));
    }
}
