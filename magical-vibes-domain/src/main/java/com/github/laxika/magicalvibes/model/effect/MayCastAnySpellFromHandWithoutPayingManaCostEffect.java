package com.github.laxika.magicalvibes.model.effect;

/**
 * "You may cast a spell from your hand without paying its mana cost." (Maelstrom Archangel)
 *
 * <p>On resolution the controller is offered each nonland hand card as a
 * may-cast-from-hand-without-paying choice via {@link MayCastFromHandWithoutPayingManaCostEffect};
 * casting one clears the rest, so only a single spell is cast. Unlike
 * {@link MayCastFromHandSharingNameWithSpellCastThisTurnEffect} there is no name filter — any
 * spell in hand is eligible.
 */
public record MayCastAnySpellFromHandWithoutPayingManaCostEffect() implements CardEffect {
}
