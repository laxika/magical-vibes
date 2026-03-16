package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromSubtypesEffect;

import java.util.Set;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "ISD", collectorNumber = "13")
public class EliteInquisitor extends Card {

    public EliteInquisitor() {
        addEffect(EffectSlot.STATIC, new ProtectionFromSubtypesEffect(Set.of(
                CardSubtype.VAMPIRE, CardSubtype.WEREWOLF, CardSubtype.ZOMBIE)));
    }
}
