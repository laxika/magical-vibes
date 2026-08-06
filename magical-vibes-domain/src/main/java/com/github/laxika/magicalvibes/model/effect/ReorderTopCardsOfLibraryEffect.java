package com.github.laxika.magicalvibes.model.effect;

/**
 * Look at the top {@code count} cards of the {@link LibraryOwner}'s library, then put them back on
 * top in any order. The effect's controller always decides the order, whichever library is
 * inspected — Index and Ponder look at their own, Portent at target player's.
 *
 * <p>A {@code count} of one needs no ordering decision, so it resolves as a look-only peek.
 */
public record ReorderTopCardsOfLibraryEffect(int count, LibraryOwner owner) implements CardEffect {

    /** Convenience constructor for the controller's own library. */
    public ReorderTopCardsOfLibraryEffect(int count) {
        this(count, LibraryOwner.CONTROLLER);
    }

    @Override
    public TargetSpec targetSpec() {
        return owner == LibraryOwner.TARGET_PLAYER
                ? TargetSpec.benign(TargetPredicates.player())
                : TargetSpec.NONE;
    }
}
