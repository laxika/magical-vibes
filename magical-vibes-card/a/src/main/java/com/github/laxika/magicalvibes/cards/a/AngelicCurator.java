package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromCardTypesEffect;

import java.util.Set;

@CardRegistration(set = "ULG", collectorNumber = "1")
@CardRegistration(set = "MH2", collectorNumber = "262")
public class AngelicCurator extends Card {

    public AngelicCurator() {
        addStaticEffect(new ProtectionFromCardTypesEffect(Set.of(CardType.ARTIFACT)));
    }
}
