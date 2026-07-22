package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

/**
 * Erupting Dreadwolf — back face of Smoldering Werewolf.
 * Whenever this creature attacks, it deals 2 damage to any target.
 */
public class EruptingDreadwolf extends Card {

    public EruptingDreadwolf() {
        // Whenever this creature attacks, it deals 2 damage to any target.
        addEffect(EffectSlot.ON_ATTACK, new DealDamageToAnyTargetEffect(2));
    }
}
