package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "ALA", collectorNumber = "104")
public class HissingIguanar extends Card {

    public HissingIguanar() {
        // Whenever another creature dies, you may have this creature deal 1 damage to
        // target player or planeswalker. ON_ANY_CREATURE_DIES excludes the source itself,
        // satisfying "another".
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new MayEffect(
                new DealDamageToTargetPlayerOrPlaneswalkerEffect(1),
                "Deal 1 damage to target player or planeswalker?"));
    }
}
