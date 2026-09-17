package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the top {@code count} cards of the selected library, then has the permission player
 * choose one of those cards to play until their next end step.
 */
public record ExileTopCardsChooseOneMayPlayUntilNextEndStepEffect(
        int count, LibraryScope libraryScope, boolean withoutPayingManaCost) implements CardEffect {

    public ExileTopCardsChooseOneMayPlayUntilNextEndStepEffect(int count) {
        this(count, LibraryScope.CONTROLLER, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return libraryScope == LibraryScope.TARGET_PLAYER
                ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
