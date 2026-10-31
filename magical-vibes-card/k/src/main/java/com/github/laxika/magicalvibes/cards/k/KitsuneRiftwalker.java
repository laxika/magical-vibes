package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromSubtypesEffect;

import java.util.Set;

@CardRegistration(set = "CHK", collectorNumber = "29")
public class KitsuneRiftwalker extends Card {

    public KitsuneRiftwalker() {
        addStaticEffect(new ProtectionFromSubtypesEffect(
                Set.of(CardSubtype.SPIRIT, CardSubtype.ARCANE)));
    }
}
