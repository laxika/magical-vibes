package com.github.laxika.magicalvibes.model.effect;

/**
 * Forces the targeted player (from the stack entry's targetPermanentId) to discard cards.
 * The targeted player chooses which cards to discard.
 *
 * @param amount number of cards to discard
 */
public record TargetPlayerDiscardsEffect(int amount) implements CardEffect {
}
