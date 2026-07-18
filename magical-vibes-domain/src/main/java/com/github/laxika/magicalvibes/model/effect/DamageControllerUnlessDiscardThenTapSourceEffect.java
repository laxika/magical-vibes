package com.github.laxika.magicalvibes.model.effect;

/**
 * Drawback: the source permanent deals {@code damage} damage to its controller unless that player
 * discards a card. If it actually deals the damage this way (the controller had no card to discard
 * or chose not to, and the damage wasn't prevented/redirected away from them), the source is tapped.
 *
 * <p>Mishra's War Machine ("At the beginning of your upkeep, this creature deals 3 damage to you
 * unless you discard a card. If it deals damage to you this way, tap it."). The damage is routed
 * through the normal damage system, and the tap fires only when the controller's life actually
 * dropped — matching the intervening "if it deals damage to you this way".
 *
 * @param damage how much damage the controller takes if they don't discard
 */
public record DamageControllerUnlessDiscardThenTapSourceEffect(int damage) implements CardEffect {
}
