package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromSubtypesEffect;

import java.util.Set;

@CardRegistration(set = "ONS", collectorNumber = "145")
public class FallenCleric extends Card {

    public FallenCleric() {
        addStaticEffect(new ProtectionFromSubtypesEffect(Set.of(CardSubtype.CLERIC)));
        addMorph("{4}{B}");
    }
}
