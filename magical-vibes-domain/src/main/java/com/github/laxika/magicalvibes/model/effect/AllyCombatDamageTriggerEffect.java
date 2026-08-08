package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Marker effect for the ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER slot.
 * When a matching creature deals combat damage to a player, the wrapped effect is put on the stack.
 * {@code bindSourceToDealer} makes the dealing creature the effect source. {@code oncePerDamageStep}
 * models the batched "one or more" wording.
 */
public record AllyCombatDamageTriggerEffect(PermanentPredicate dealerPredicate, CardEffect effect,
                                            boolean bindSourceToDealer,
                                            boolean oncePerDamageStep) implements CardEffect {

    public AllyCombatDamageTriggerEffect(PermanentPredicate dealerPredicate, CardEffect effect) {
        this(dealerPredicate, effect, false, false);
    }

    public AllyCombatDamageTriggerEffect(PermanentPredicate dealerPredicate, CardEffect effect,
                                         boolean bindSourceToDealer) {
        this(dealerPredicate, effect, bindSourceToDealer, false);
    }

    /** Compatibility accessor for the graveyard-trigger terminology. */
    public boolean oneOrMoreDealers() {
        return oncePerDamageStep;
    }
}
