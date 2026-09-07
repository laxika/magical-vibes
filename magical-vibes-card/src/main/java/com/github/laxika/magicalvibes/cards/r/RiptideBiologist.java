package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromSubtypesEffect;

import java.util.Set;

@CardRegistration(set = "ONS", collectorNumber = "106")
public class RiptideBiologist extends Card {

    public RiptideBiologist() {
        addEffect(EffectSlot.STATIC, new ProtectionFromSubtypesEffect(Set.of(CardSubtype.BEAST)));
        addMorph("{2}{U}");
    }
}
