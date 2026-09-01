package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Looks at the top card of the controller's library. A land may be played, or a spell whose mana
 * value is at most {@code maxManaValue} may be cast without paying its mana cost. If neither
 * option is taken, the card is put into its controller's hand.
 */
public record LookAtTopCardMayPlayLandOrCastFreeEffect(DynamicAmount maxManaValue)
        implements CombatDamageAmountAwareEffect {

    public LookAtTopCardMayPlayLandOrCastFreeEffect(int maxManaValue) {
        this(new Fixed(maxManaValue));
    }

    @Override
    public DynamicAmount combatDamageAmount() {
        return maxManaValue;
    }
}
