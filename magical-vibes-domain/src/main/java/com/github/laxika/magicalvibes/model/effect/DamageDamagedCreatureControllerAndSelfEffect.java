package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker effect for the {@code ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE} slot, self-scoped
 * (Bellowing Fiend).
 *
 * <p>"Whenever this creature deals damage to a creature, this creature deals
 * {@code amountToDamagedCreatureController} damage to that creature's controller and
 * {@code amountToSelf} damage to you." Fires only when the watcher itself is the damage source.
 * The trigger collector expands each match into two {@link DealDamageToPlayersEffect}s
 * ({@code TARGET_PLAYER} for the damaged creature's controller, {@code CONTROLLER} for you) on a
 * single stack entry — the marker itself is never resolved.
 */
public record DamageDamagedCreatureControllerAndSelfEffect(int amountToDamagedCreatureController,
                                                           int amountToSelf) implements CardEffect {
}
