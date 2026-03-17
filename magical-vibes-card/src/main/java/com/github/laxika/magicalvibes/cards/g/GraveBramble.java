package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromSubtypesEffect;

import java.util.Set;

@CardRegistration(set = "ISD", collectorNumber = "184")
public class GraveBramble extends Card {

    public GraveBramble() {
        addEffect(EffectSlot.STATIC, new ProtectionFromSubtypesEffect(Set.of(CardSubtype.ZOMBIE)));
    }
}
