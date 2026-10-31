package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;

import java.util.Set;

@CardRegistration(set = "NEM", collectorNumber = "25")
public class VoiceOfTruth extends Card {

    public VoiceOfTruth() {
        addStaticEffect(new ProtectionFromColorsEffect(Set.of(CardColor.WHITE)));
    }
}
