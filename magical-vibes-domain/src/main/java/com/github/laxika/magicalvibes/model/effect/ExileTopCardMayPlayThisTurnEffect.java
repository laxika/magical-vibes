package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Exile the top {@code count} cards of a library ({@code library}, the controller's by default: "that player
 * exiles the top two cards of their library", "exile the top card of target opponent's library"). For
 * {@code duration} (until end of turn by default) the controller, or the effect's {@code player}, may play those cards
 * (any type, lands included). When {@code withoutPayingManaCost} is
 * {@code true} the play is free; otherwise it is played at its normal costs and timing. When
 * {@code freeCastFilter} is non-null, only matching cards are free; all other exiled cards retain
 * normal play permission (Nahiri, Forged in Fury). The count is evaluated when the effect resolves.
 * <p>
 * Grants an {@link com.github.laxika.magicalvibes.model.effect.ExilePlayPermissionEffect} for {@code duration} (and, for the
 * free variant, {@code exilePlayWithoutPayingManaCost}). The filter is used for mixed normal/free
 * permission effects; use {@link ExileTopCardsMayCastMatchingThisTurnEffect} when non-matching
 * cards should not receive play permission at all.
 *
 * @param library     whose library: each player it names exiles from their own, in APNAP order; {@code targetPlayer()}
 *                    declares the (harmful) player target
 * @param castOnly    "you may cast that card": a land card gets no permission (Ragavan, Daxos)
 * @param anyManaType "and you may spend mana as though it were mana of any color to cast that spell": for each
 *                    nonland card exiled this way, for as long as the permission lasts
 * @param duration    how long the permission lasts: {@link Duration#UNTIL_END_OF_TURN} ("this turn"),
 *                    {@link Duration#UNTIL_YOUR_NEXT_END_STEP}, {@link Duration#UNTIL_END_OF_YOUR_NEXT_TURN},
 *                    {@link Duration#UNTIL_CONTROLLERS_NEXT_UPKEEP}, {@link Duration#DURING_YOUR_NEXT_TURN} ("during
 *                    your next turn"), {@link Duration#PERMANENT} ("for as long as it remains exiled"); "your" is the
 *                    {@code player}'s
 * @param faceDown    "exile ... face down. You may look at and play that card": the {@code player} may look at it
 * @param forLife     "if you cast a spell this way, pay life equal to its mana value rather than pay its mana cost"
 * @param player      who may play the cards: {@code you()} by default, another player read without a target ("they may
 *                    play those cards", "that creature's controller may play that card"); nobody named, nothing is
 *                    exiled
 * @param permissionCondition when non-null, the cards may be played only while it holds ("during any turn you attacked
 *                    with a token", "as long as you've cast another spell this turn"), judged as the permission is used
 */
public record ExileTopCardMayPlayThisTurnEffect(
        DynamicAmount count,
        boolean withoutPayingManaCost,
        CardPredicate freeCastFilter,
        boolean duringTurnsLoreCounterPutOnSource,
        PlayerSubject library,
        boolean castOnly,
        boolean anyManaType,
        Duration duration,
        boolean faceDown,
        boolean forLife,
        PlayerSubject player,
        Condition permissionCondition
) implements ExileTopCardsMayPlayThisTurnDrawReplacementEffect {

    public ExileTopCardMayPlayThisTurnEffect {
        if (library == null) {
            throw new IllegalArgumentException("ExileTopCardMayPlayThisTurnEffect requires a library subject");
        }
        if (duration == null) {
            throw new IllegalArgumentException("ExileTopCardMayPlayThisTurnEffect requires a permission duration");
        }
        if (player == null || player.declaresTarget()) {
            throw new IllegalArgumentException("ExileTopCardMayPlayThisTurnEffect requires a non-targeting player");
        }
    }

    public ExileTopCardMayPlayThisTurnEffect(
            DynamicAmount count,
            boolean withoutPayingManaCost,
            CardPredicate freeCastFilter,
            boolean duringTurnsLoreCounterPutOnSource,
            PlayerSubject library,
            boolean castOnly,
            boolean anyManaType
    ) {
        this(count, withoutPayingManaCost, freeCastFilter, duringTurnsLoreCounterPutOnSource, library, castOnly,
                anyManaType, Duration.UNTIL_END_OF_TURN, false, false, Subjects.you(), null);
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
                duringTurnsLoreCounterPutOnSource, library, castOnly, anyManaType, duration, faceDown, forLife,
                player, permissionCondition);
    }

    /** "You may cast that card": no permission for a land card. */
    public ExileTopCardMayPlayThisTurnEffect mayOnlyCast() {
        return new ExileTopCardMayPlayThisTurnEffect(count, withoutPayingManaCost, freeCastFilter,
                duringTurnsLoreCounterPutOnSource, library, true, anyManaType, duration, faceDown, forLife,
                player, permissionCondition);
    }

    /** "And you may spend mana as though it were mana of any color to cast that spell." */
    public ExileTopCardMayPlayThisTurnEffect withAnyManaType() {
        return new ExileTopCardMayPlayThisTurnEffect(count, withoutPayingManaCost, freeCastFilter,
                duringTurnsLoreCounterPutOnSource, library, castOnly, true, duration, faceDown, forLife,
                player, permissionCondition);
    }

    /** The permission lasts for {@code duration} instead of until end of turn ("until your next end step"). */
    public ExileTopCardMayPlayThisTurnEffect until(Duration duration) {
        return new ExileTopCardMayPlayThisTurnEffect(count, withoutPayingManaCost, freeCastFilter,
                duringTurnsLoreCounterPutOnSource, library, castOnly, anyManaType, duration, faceDown, forLife,
                player, permissionCondition);
    }

    /** "Exile ... face down. You may look at and play that card." */
    public ExileTopCardMayPlayThisTurnEffect exiledFaceDown() {
        return new ExileTopCardMayPlayThisTurnEffect(count, withoutPayingManaCost, freeCastFilter,
                duringTurnsLoreCounterPutOnSource, library, castOnly, anyManaType, duration, true, forLife,
                player, permissionCondition);
    }

    /** "If you cast a spell this way, pay life equal to its mana value rather than pay its mana cost." */
    public ExileTopCardMayPlayThisTurnEffect payingLifeEqualToManaValue() {
        return new ExileTopCardMayPlayThisTurnEffect(count, withoutPayingManaCost, freeCastFilter,
                duringTurnsLoreCounterPutOnSource, library, castOnly, anyManaType, duration, faceDown, true,
                player, permissionCondition);
    }

    /** The cards may be played by {@code player} instead of the controller ("they may play those cards"). */
    public ExileTopCardMayPlayThisTurnEffect playableBy(PlayerSubject player) {
        return new ExileTopCardMayPlayThisTurnEffect(count, withoutPayingManaCost, freeCastFilter,
                duringTurnsLoreCounterPutOnSource, library, castOnly, anyManaType, duration, faceDown, forLife, player,
                permissionCondition);
    }

    /** The cards may be played only while {@code condition} holds ("during any turn you attacked with a token"). */
    public ExileTopCardMayPlayThisTurnEffect playableOnlyIf(Condition condition) {
        return new ExileTopCardMayPlayThisTurnEffect(count, withoutPayingManaCost, freeCastFilter,
                duringTurnsLoreCounterPutOnSource, library, castOnly, anyManaType, duration, faceDown, forLife, player,
                condition);
    }

    @Override
    public TargetSpec targetSpec() {
        return library.declaresTarget() ? TargetSpec.harmful(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
