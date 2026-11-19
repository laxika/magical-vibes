package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the top {@code count} cards of the selected library, then lets the permission player play
 * one of those cards until their next end step.
 *
 * @param count the number of cards to exile
 * @param player whose library the cards are exiled from; that player gets the permission unless it is
 *               {@code you()} ({@code targetPlayer()} declares a benign player target)
 * @param withoutPayingManaCost {@code true} when the played card is cast or played without paying its
 *                              mana cost
 * @param chooseOnExile {@code true} when the permission player must choose the one playable card as the
 *                      effect resolves; {@code false} when every exiled card is playable but only one of
 *                      them may be played ("until your next end step, you may play one of those cards")
 */
public record ExileTopCardsChooseOneMayPlayUntilNextEndStepEffect(
        int count, PlayerSubject player, boolean withoutPayingManaCost, boolean chooseOnExile)
        implements CardEffect {

    public ExileTopCardsChooseOneMayPlayUntilNextEndStepEffect(int count) {
        this(count, Subjects.you(), false, true);
    }

    public ExileTopCardsChooseOneMayPlayUntilNextEndStepEffect(int count, PlayerSubject player,
                                                               boolean withoutPayingManaCost) {
        this(count, player, withoutPayingManaCost, true);
    }

    public static ExileTopCardsChooseOneMayPlayUntilNextEndStepEffect mayPlayOneOfThem(int count) {
        return new ExileTopCardsChooseOneMayPlayUntilNextEndStepEffect(count, Subjects.you(), false, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return player.declaresTarget()
                ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
