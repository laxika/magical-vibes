package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Look at the top {@code count} cards of the {@link LibraryOwner}'s library, then put them back on
 * top in any order. The {@link LibraryDecisionMaker} decides the order, whichever library is
 * inspected.
 *
 * <p>A {@code count} of one needs no ordering decision, so it resolves as a look-only peek.
 */
public record ReorderTopCardsOfLibraryEffect(DynamicAmount count, LibraryOwner owner,
                                             LibraryDecisionMaker decisionMaker) implements CardEffect {
    /** Convenience constructor using the controller as the decision-maker. */
    public ReorderTopCardsOfLibraryEffect(DynamicAmount count, LibraryOwner owner) {
        this(count, owner, LibraryDecisionMaker.CONTROLLER);
    }

    /** Convenience constructor for a fixed number of cards in a specified library. */
    public ReorderTopCardsOfLibraryEffect(int count, LibraryOwner owner) {
        this(new Fixed(count), owner, LibraryDecisionMaker.CONTROLLER);
    }

    /** Convenience constructor for a fixed number of cards with an explicit decision-maker. */
    public ReorderTopCardsOfLibraryEffect(int count, LibraryOwner owner,
                                          LibraryDecisionMaker decisionMaker) {
        this(new Fixed(count), owner, decisionMaker);
    }

    /** Convenience constructor for a dynamic count in the controller's own library. */
    public ReorderTopCardsOfLibraryEffect(DynamicAmount count) {
        this(count, LibraryOwner.CONTROLLER, LibraryDecisionMaker.CONTROLLER);
    }

    /** Convenience constructor for a fixed number of cards in the controller's own library. */
    public ReorderTopCardsOfLibraryEffect(int count) {
        this(new Fixed(count), LibraryOwner.CONTROLLER, LibraryDecisionMaker.CONTROLLER);
    }

    @Override
    public TargetSpec targetSpec() {
        return owner == LibraryOwner.TARGET_PLAYER || decisionMaker == LibraryDecisionMaker.TARGET_PLAYER
                ? TargetSpec.benign(TargetPredicates.player())
                : TargetSpec.NONE;
    }
}
