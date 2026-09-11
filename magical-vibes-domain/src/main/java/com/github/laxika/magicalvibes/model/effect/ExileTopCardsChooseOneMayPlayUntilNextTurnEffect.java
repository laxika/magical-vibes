package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Exiles the top {@code count} cards of a library, then has the effect controller choose one of
 * those cards to play until the end of their next turn.
 *
 * @param count how many cards are exiled from the top of the library
 * @param libraryScope whose library is exiled
 * @param anyManaType whether the chosen card may be cast using mana of any color
 */
public record ExileTopCardsChooseOneMayPlayUntilNextTurnEffect(
        DynamicAmount count, LibraryScope libraryScope, boolean anyManaType) implements CardEffect {

    public ExileTopCardsChooseOneMayPlayUntilNextTurnEffect(DynamicAmount count) {
        this(count, LibraryScope.CONTROLLER, false);
    }

    public ExileTopCardsChooseOneMayPlayUntilNextTurnEffect(int count) {
        this(new Fixed(count));
    }

    public ExileTopCardsChooseOneMayPlayUntilNextTurnEffect(int count, LibraryScope libraryScope,
                                                            boolean anyManaType) {
        this(new Fixed(count), libraryScope, anyManaType);
    }

    @Override
    public TargetSpec targetSpec() {
        return libraryScope == LibraryScope.TARGET_PLAYER
                ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
