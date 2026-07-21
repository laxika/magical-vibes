package com.github.laxika.magicalvibes.model.effect;

/**
 * Target player reveals cards from the top of their library until {@code landCount} land cards are
 * revealed (or the library empties). That player puts all cards revealed this way into their
 * graveyard.
 * <p>
 * Used by Mind Funeral ({@code landCount = 4}). Targets a player (the card restricts the choice to
 * an opponent).
 */
public record RevealUntilLandsMillTargetPlayerEffect(int landCount) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.PLAYER);
    }
}
