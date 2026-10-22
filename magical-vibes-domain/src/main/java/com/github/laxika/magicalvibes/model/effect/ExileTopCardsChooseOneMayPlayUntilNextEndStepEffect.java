package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the top {@code count} cards of the selected library, then lets the permission player play
 * one of those cards until their next end step.
 *
 * @param count the number of cards to exile
 * @param libraryScope whose library the cards are exiled from
 * @param withoutPayingManaCost {@code true} when the played card is cast or played without paying its
 *                              mana cost
 * @param chooseOnExile {@code true} when the permission player must choose the one playable card as the
 *                      effect resolves; {@code false} when every exiled card is playable but only one of
 *                      them may be played ("until your next end step, you may play one of those cards")
 */
public record ExileTopCardsChooseOneMayPlayUntilNextEndStepEffect(
        int count, LibraryScope libraryScope, boolean withoutPayingManaCost, boolean chooseOnExile)
        implements CardEffect {

    public ExileTopCardsChooseOneMayPlayUntilNextEndStepEffect(int count) {
        this(count, LibraryScope.CONTROLLER, false, true);
    }

    public ExileTopCardsChooseOneMayPlayUntilNextEndStepEffect(int count, LibraryScope libraryScope,
                                                               boolean withoutPayingManaCost) {
        this(count, libraryScope, withoutPayingManaCost, true);
    }

    public static ExileTopCardsChooseOneMayPlayUntilNextEndStepEffect mayPlayOneOfThem(int count) {
        return new ExileTopCardsChooseOneMayPlayUntilNextEndStepEffect(count, LibraryScope.CONTROLLER, false, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return libraryScope == LibraryScope.TARGET_PLAYER
                ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
