package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Exile the top {@code count} cards of a library ({@code library}, the controller's by default: "that player
 * exiles the top two cards of their library", "exile the top card of target opponent's library"). Until end of turn,
 * the controller may play those cards (any type, lands included). When {@code withoutPayingManaCost} is
 * {@code true} the play is free; otherwise it is played at its normal costs and timing. When
 * {@code freeCastFilter} is non-null, only matching cards are free; all other exiled cards retain
 * normal play permission (Nahiri, Forged in Fury). The count is evaluated when the effect resolves.
 * <p>
 * Grants an {@link com.github.laxika.magicalvibes.model.effect.ExilePlayPermissionEffect} until end of turn (and, for the
 * free variant, {@code exilePlayWithoutPayingManaCost}). The filter is used for mixed normal/free
 * permission effects; use {@link ExileTopCardsMayCastMatchingThisTurnEffect} when non-matching
 * cards should not receive play permission at all.
 *
 * @param library     whose library: each player it names exiles from their own, in APNAP order; {@code targetPlayer()}
 *                    declares the (harmful) player target
 * @param castOnly    "you may cast that card": a land card gets no permission (Ragavan, Daxos)
 * @param anyManaType "and you may spend mana as though it were mana of any color to cast that spell": for each
 *                    nonland card exiled this way
 */
public record ExileTopCardMayPlayThisTurnEffect(
        DynamicAmount count,
        boolean withoutPayingManaCost,
        CardPredicate freeCastFilter,
        boolean duringTurnsLoreCounterPutOnSource,
        PlayerSubject library,
        boolean castOnly,
        boolean anyManaType
) implements ExileTopCardsMayPlayThisTurnDrawReplacementEffect {

    public ExileTopCardMayPlayThisTurnEffect {
        if (library == null) {
            throw new IllegalArgumentException("ExileTopCardMayPlayThisTurnEffect requires a library subject");
        }
    }

    public ExileTopCardMayPlayThisTurnEffect(
            DynamicAmount count,
            boolean withoutPayingManaCost,
            CardPredicate freeCastFilter,
            boolean duringTurnsLoreCounterPutOnSource
    ) {
        this(count, withoutPayingManaCost, freeCastFilter, duringTurnsLoreCounterPutOnSource, Subjects.you(),
                false, false);
    }

    /**
     * Canonical-shaped constructor for the usual "until end of turn" form.
     */
    public ExileTopCardMayPlayThisTurnEffect(
            DynamicAmount count,
            boolean withoutPayingManaCost,
            CardPredicate freeCastFilter
    ) {
        this(count, withoutPayingManaCost, freeCastFilter, false);
    }

    /**
     * Single-card variant whose play permission is renewed during any turn the controller puts a lore
     * counter on the source Saga, even once the Saga has been sacrificed (Summon: Brynhildr).
     */
    public static ExileTopCardMayPlayThisTurnEffect renewedOnLoreCounterTurns() {
        return new ExileTopCardMayPlayThisTurnEffect(new Fixed(1), false, null, true);
    }

    /** Single-card variant (Oracle's Vault). */
    public ExileTopCardMayPlayThisTurnEffect(boolean withoutPayingManaCost) {
        this(new Fixed(1), withoutPayingManaCost, null);
    }

    public ExileTopCardMayPlayThisTurnEffect(int count, boolean withoutPayingManaCost) {
        this(new Fixed(count), withoutPayingManaCost, null);
    }

    public ExileTopCardMayPlayThisTurnEffect(DynamicAmount count, boolean withoutPayingManaCost) {
        this(count, withoutPayingManaCost, null);
    }

    public ExileTopCardMayPlayThisTurnEffect(
            int count,
            boolean withoutPayingManaCost,
            CardPredicate freeCastFilter
    ) {
        this(new Fixed(count), withoutPayingManaCost, freeCastFilter);
    }

    /** The same effect on {@code library}'s library ("that player exiles the top card of their library"). */
    public ExileTopCardMayPlayThisTurnEffect fromLibraryOf(PlayerSubject library) {
        return new ExileTopCardMayPlayThisTurnEffect(count, withoutPayingManaCost, freeCastFilter,
                duringTurnsLoreCounterPutOnSource, library, castOnly, anyManaType);
    }

    /** "You may cast that card": no permission for a land card. */
    public ExileTopCardMayPlayThisTurnEffect mayOnlyCast() {
        return new ExileTopCardMayPlayThisTurnEffect(count, withoutPayingManaCost, freeCastFilter,
                duringTurnsLoreCounterPutOnSource, library, true, anyManaType);
    }

    /** "And you may spend mana as though it were mana of any color to cast that spell." */
    public ExileTopCardMayPlayThisTurnEffect withAnyManaType() {
        return new ExileTopCardMayPlayThisTurnEffect(count, withoutPayingManaCost, freeCastFilter,
                duringTurnsLoreCounterPutOnSource, library, castOnly, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return library.declaresTarget() ? TargetSpec.harmful(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
