package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect that exiles creatures instead of letting them die and makes each
 * replaced creature's controller lose the configured amount of life.
 */
public record ExileCreaturesInsteadOfDyingWithLifeLossEffect(int lifeLoss) implements CardEffect {
}
