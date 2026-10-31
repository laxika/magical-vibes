package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromSubtypesEffect;

import java.util.Set;

@CardRegistration(set = "LRW", collectorNumber = "129")
public class NathsBuffoon extends Card {

    public NathsBuffoon() {
        addStaticEffect(new ProtectionFromSubtypesEffect(Set.of(CardSubtype.ELF)));
    }
}
