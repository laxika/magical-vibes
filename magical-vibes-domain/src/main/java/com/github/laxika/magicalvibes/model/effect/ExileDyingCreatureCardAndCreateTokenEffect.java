package com.github.laxika.magicalvibes.model.effect;

/**
 * Unscythe, Killer of Kings: "you may exile that card. If you do, create a 2/2 black Zombie creature
 * token." Placed (wrapped in a {@link MayEffect}) in the {@code Triggers.creatureDies(DAMAGED_BY_THIS)} ability.
 *
 * <p>The dying creature's card is the stack entry's triggering card; the {@code token} blueprint is
 * supplied by the card. Resolution exiles the dying card from its owner's graveyard only if it is
 * still there — per the official rulings, a token or a card that has already left the graveyard can't
 * be exiled, and if the exile doesn't happen no token is created.
 *
 * @param token the token to create when the exile succeeds
 */
public record ExileDyingCreatureCardAndCreateTokenEffect(CreateTokenEffect token) implements CardEffect {
}
