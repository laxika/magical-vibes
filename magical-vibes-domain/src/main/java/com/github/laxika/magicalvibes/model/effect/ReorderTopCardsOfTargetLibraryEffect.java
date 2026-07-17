package com.github.laxika.magicalvibes.model.effect;

/**
 * Look at the top {@code count} cards of target player's library, then put them back on top in any
 * order (the controller decides the order). Target-player variant of
 * {@link ReorderTopCardsOfLibraryEffect} — e.g. Portent, which looks at the top three cards of
 * target player's library.
 */
public record ReorderTopCardsOfTargetLibraryEffect(int count) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER);
    }
}
