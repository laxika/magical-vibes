package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;

import java.util.Set;

@CardRegistration(set = "INV", collectorNumber = "286")
public class VodalianZombie extends Card {

    public VodalianZombie() {
        addStaticEffect(new ProtectionFromColorsEffect(Set.of(CardColor.GREEN)));
    }
}
