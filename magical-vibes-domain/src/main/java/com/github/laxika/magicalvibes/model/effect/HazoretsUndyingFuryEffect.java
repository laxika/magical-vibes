package com.github.laxika.magicalvibes.model.effect;

/**
 * Shuffle your library, then exile the top {@code exileCount} cards. You may cast any number of
 * spells with mana value {@code maxCastableManaValue} or less from among them without paying their
 * mana costs. Cards not cast this way stay exiled.
 *
 * <p>Reuses the Improvisation Capstone cast machinery: after exiling, this queues an
 * {@code ImprovisationCapstoneCastChoice} interaction over the eligible exiled spells.</p>
 */
public record HazoretsUndyingFuryEffect(int exileCount, int maxCastableManaValue) implements CardEffect {
}
