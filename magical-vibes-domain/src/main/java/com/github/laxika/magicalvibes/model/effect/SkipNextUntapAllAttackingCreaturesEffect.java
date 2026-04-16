package com.github.laxika.magicalvibes.model.effect;

/**
 * Effect: "Those creatures don't untap during their controller's next untap step."
 * (where "those creatures" = all attacking creatures)
 *
 * <p>Iterates all permanents across all battlefields, increments
 * {@code skipUntapCount} on each creature that is currently attacking.</p>
 */
public record SkipNextUntapAllAttackingCreaturesEffect() implements CardEffect {
}
