package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Exiles the top {@code count} cards of a library, then has the effect controller choose one of
 * those cards to play until the end of their next turn.
 *
 * @param count how many cards are exiled from the top of the library
 * @param player whose library is exiled ({@code targetPlayer()} declares a benign player target)
 * @param anyManaType whether the chosen card may be cast using mana of any color
 */
public record ExileTopCardsChooseOneMayPlayUntilNextTurnEffect(
        DynamicAmount count, PlayerSubject player, boolean anyManaType) implements CardEffect {

    public ExileTopCardsChooseOneMayPlayUntilNextTurnEffect(DynamicAmount count) {
        this(count, Subjects.you(), false);
    }

    public ExileTopCardsChooseOneMayPlayUntilNextTurnEffect(int count) {
        this(new Fixed(count));
    }

    public ExileTopCardsChooseOneMayPlayUntilNextTurnEffect(int count, PlayerSubject player,
                                                            boolean anyManaType) {
        this(new Fixed(count), player, anyManaType);
    }

    @Override
    public TargetSpec targetSpec() {
        return player.declaresTarget()
                ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
