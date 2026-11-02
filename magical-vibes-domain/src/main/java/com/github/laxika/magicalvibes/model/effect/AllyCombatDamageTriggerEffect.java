package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Marker effect for the Triggers.creatureYouControlDealsCombatDamageToPlayer() ability.
 * When a matching creature deals combat damage to a player, the wrapped effect is put on the stack.
 * {@code bindSourceToDealer} makes the dealing creature the effect source. {@code oncePerDamageStep}
 * models the batched "one or more" wording. {@code opponentDealtDamage} models "whenever your
 * opponents are dealt combat damage" wording: the dealer may be controlled by any player, but the damaged
 * player must be an opponent of the trigger's controller.
 */
public record AllyCombatDamageTriggerEffect(PermanentPredicate dealerPredicate, CardEffect effect,
                                            boolean bindSourceToDealer,
                                            boolean oncePerDamageStep,
                                            boolean opponentDealtDamage) implements CardEffect {

    public AllyCombatDamageTriggerEffect(PermanentPredicate dealerPredicate, CardEffect effect,
                                         boolean bindSourceToDealer, boolean oncePerDamageStep) {
        this(dealerPredicate, effect, bindSourceToDealer, oncePerDamageStep, false);
    }

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
