package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;

@CardRegistration(set = "7ED", collectorNumber = "172")
public class AetherFlash extends Card {

    public AetherFlash() {
        // Whenever a creature enters, this enchantment deals 2 damage to it.
        addEffect(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD,
                new DealDamageToTargetCreatureEffect(2));
    }
}
