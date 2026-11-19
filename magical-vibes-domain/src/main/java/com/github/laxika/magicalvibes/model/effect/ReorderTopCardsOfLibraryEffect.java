package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Look at the top {@code count} cards of the library of the player {@code player} names (the controller's own when
 * it names nobody), then put them back on top in any order. The {@link LibraryDecisionMaker} decides the order,
 * whichever library is inspected. Only {@code targetPlayer()} or a target-player decision-maker declares a (benign)
 * player target.
 *
 * <p>A {@code count} of one needs no ordering decision, so it resolves as a look-only peek.
 */
public record ReorderTopCardsOfLibraryEffect(DynamicAmount count, PlayerSubject player,
                                             LibraryDecisionMaker decisionMaker) implements CardEffect {
    /** Convenience constructor using the controller as the decision-maker. */
    public ReorderTopCardsOfLibraryEffect(DynamicAmount count, PlayerSubject player) {
        this(count, player, LibraryDecisionMaker.CONTROLLER);
    }

    /** Convenience constructor for a fixed number of cards in a specified library. */
    public ReorderTopCardsOfLibraryEffect(int count, PlayerSubject player) {
        this(new Fixed(count), player, LibraryDecisionMaker.CONTROLLER);
    }

    /** Convenience constructor for a fixed number of cards with an explicit decision-maker. */
    public ReorderTopCardsOfLibraryEffect(int count, PlayerSubject player,
                                          LibraryDecisionMaker decisionMaker) {
        this(new Fixed(count), player, decisionMaker);
    }

    /** Convenience constructor for a dynamic count in the controller's own library. */
    public ReorderTopCardsOfLibraryEffect(DynamicAmount count) {
        this(count, Subjects.you(), LibraryDecisionMaker.CONTROLLER);
    }

    /** Convenience constructor for a fixed number of cards in the controller's own library. */
    public ReorderTopCardsOfLibraryEffect(int count) {
        this(new Fixed(count), Subjects.you(), LibraryDecisionMaker.CONTROLLER);
    }

    @Override
    public TargetSpec targetSpec() {
        return player.declaresTarget() || decisionMaker == LibraryDecisionMaker.TARGET_PLAYER
                ? TargetSpec.benign(TargetPredicates.player())
                : TargetSpec.NONE;
    }
}
