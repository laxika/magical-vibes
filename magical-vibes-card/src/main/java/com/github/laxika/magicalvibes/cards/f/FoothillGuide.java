package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromSubtypesEffect;

import java.util.Set;

@CardRegistration(set = "ONS", collectorNumber = "29")
public class FoothillGuide extends Card {

    public FoothillGuide() {
        addEffect(EffectSlot.STATIC, new ProtectionFromSubtypesEffect(Set.of(CardSubtype.GOBLIN)));
        addMorph("{W}");
    }
}
