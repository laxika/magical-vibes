package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromCardTypesEffect;

import java.util.Set;

@CardRegistration(set = "MRD", collectorNumber = "131")
public class TelJiladArchers extends Card {

    public TelJiladArchers() {
        // Reach is auto-loaded from Scryfall.
        addStaticEffect(new ProtectionFromCardTypesEffect(Set.of(CardType.ARTIFACT)));
    }
}
