package com.github.laxika.magicalvibes.model.effect;

/**
 * The enchanted permanent's controller (the trigger's baked {@code targetId}) discards a card at
 * random; only if a card was actually discarded, the permanent the source Aura is attached to
 * untaps.
 *
 * <p>Models the "if the player does" half of Apathy: "At the beginning of the upkeep of enchanted
 * creature's controller, that player may discard a card at random. If the player does, untap that
 * creature." Wrap it in {@code MayPayManaEffect(null, …, MayPayPayer.ENCHANTED_CONTROLLER)} on the
 * {@code ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED} slot so the "may" is offered to the
 * enchanted creature's controller (the Dance of the Dead pay-to-untap shape with a random discard
 * instead of a mana cost).
 *
 * <p>An empty hand means no discard happens, so the creature stays tapped — which is why this is a
 * single effect rather than a {@code DiscardEffect} plus {@link UntapEquippedCreatureEffect}.
 */
public record DiscardRandomCardThenUntapEnchantedCreatureEffect() implements CardEffect {
}
