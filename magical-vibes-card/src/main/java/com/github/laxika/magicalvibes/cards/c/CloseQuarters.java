package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "MMQ", collectorNumber = "184")
public class CloseQuarters extends Card {

    public CloseQuarters() {
        // Whenever a creature you control becomes blocked, deal 1 damage to any target.
        addEffect(EffectSlot.ON_ALLY_CREATURE_BECOMES_BLOCKED, new DealDamageToAnyTargetEffect(1));
    }
}
