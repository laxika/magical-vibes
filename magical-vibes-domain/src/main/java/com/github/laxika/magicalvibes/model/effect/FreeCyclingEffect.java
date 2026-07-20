package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability marker for a static effect that lets its controller pay {0} rather than a cycling
 * ability's mana cost, as long as they have at least {@link #minCardsInHand()} cards in hand
 * (New Perspectives). This is an alternative cost per CR 118.9; only the mana cost is replaced —
 * the "discard this card" cost of cycling is still paid.
 *
 * <p>Placed on {@code EffectSlot.STATIC} and read directly at cycling activation
 * ({@code AbilityActivationService}), mirroring how {@link AlternativeCostForSpellsEffect} is
 * consulted for spells. Lets that consumer ask the fact "does this permanent grant free cycling"
 * without knowing the concrete effect type. The card being cycled still counts toward the threshold.
 */
public interface FreeCyclingEffect extends CardEffect {

    /** Minimum cards the controller must have in hand for cycling costs to become {0}. */
    int minCardsInHand();
}
