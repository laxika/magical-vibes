package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Look at the top {@code count} cards of the {@link LibraryOwner}'s library, then put them back on
 * top in any order. The effect's controller always decides the order, whichever library is
 * inspected — Index and Ponder look at their own, Portent at target player's.
 *
 * <p>A {@code count} of one needs no ordering decision, so it resolves as a look-only peek.
 */
public record ReorderTopCardsOfLibraryEffect(DynamicAmount count, LibraryOwner owner) implements CardEffect {

    /** Convenience constructor for a fixed number of cards in a specified library. */
    public ReorderTopCardsOfLibraryEffect(int count, LibraryOwner owner) {
        this(new Fixed(count), owner);
    }

    /** Convenience constructor for a dynamic count in the controller's own library. */
    public ReorderTopCardsOfLibraryEffect(DynamicAmount count) {
        this(count, LibraryOwner.CONTROLLER);
    }

    /** Convenience constructor for the controller's own library. */
    public ReorderTopCardsOfLibraryEffect(int count) {
        this(new Fixed(count), LibraryOwner.CONTROLLER);
    }

    @Override
    public TargetSpec targetSpec() {
        return owner == LibraryOwner.TARGET_PLAYER
                ? TargetSpec.benign(TargetPredicates.player())
                : TargetSpec.NONE;
    }
}
