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
 * free variant, {@code exilePlayWithoutPayingManaCost}). The free filter is used for mixed normal/free
 * permission effects; {@code castFilter} ({@link #mayOnlyCast(CardPredicate)}) gives no permission at all to cards
 * that can't be cast as a matching spell.
 *
 * @param library     whose library: each player it names exiles from their own, in APNAP order; {@code targetPlayer()}
 *                    declares the (harmful) player target
 * @param castOnly    "you may cast that card": a land card gets no permission (Ragavan, Daxos)
 * @param anyManaType "and you may spend mana as though it were mana of any color to cast that spell": for each
 *                    card exiled this way that can be cast (a nonland card, or a land card's Adventure), for as long
 *                    as the permission lasts
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
 * @param castFilter  "you may cast [red | instant or sorcery] spells from among them", "if it's a nonland card, you may
 *                    cast it": only a card that can be cast as a matching spell gets the permission (its front face, a
 *                    modal double-faced card's back face or an Adventure: Chandra, Hope's Beacon's ruling), and the
 *                    permission casts only matching spells; {@code null} for no filter
 * @param playLimit   "you may play one of those cards", "up to two of those cards", "an instant or sorcery spell from
 *                    among those cards": how many of the permitted cards may be played in all; 0 for no limit
 * @param chooseOne   "choose one of them. You may play that card": the {@code player} chooses one exiled card as the
 *                    effect resolves (a prompt at the end of the step) and only it gets the permission
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
        Condition permissionCondition,
        CardPredicate castFilter,
        int playLimit,
        boolean chooseOne
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
        if (playLimit < 0) {
            throw new IllegalArgumentException("A play limit is not negative: " + playLimit);
        }
        if (chooseOne && (playLimit != 0 || castOnly || castFilter != null || faceDown || forLife
                || permissionCondition != null || freeCastFilter != null || duringTurnsLoreCounterPutOnSource)) {
            throw new IllegalArgumentException("A chosen card's permission takes only a duration, a holder, free play "
                    + "and any-type mana");
        }
    }

    public ExileTopCardMayPlayThisTurnEffect(
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
    ) {
        this(count, withoutPayingManaCost, freeCastFilter, duringTurnsLoreCounterPutOnSource, library, castOnly,
                anyManaType, duration, faceDown, forLife, player, permissionCondition, null, 0, false);
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
                player, permissionCondition, castFilter, playLimit, chooseOne);
    }

    /** "You may cast that card": no permission for a land card. */
    public ExileTopCardMayPlayThisTurnEffect mayOnlyCast() {
        return new ExileTopCardMayPlayThisTurnEffect(count, withoutPayingManaCost, freeCastFilter,
                duringTurnsLoreCounterPutOnSource, library, true, anyManaType, duration, faceDown, forLife,
                player, permissionCondition, castFilter, playLimit, chooseOne);
    }

    /** "And you may spend mana as though it were mana of any color to cast that spell." */
    public ExileTopCardMayPlayThisTurnEffect withAnyManaType() {
        return new ExileTopCardMayPlayThisTurnEffect(count, withoutPayingManaCost, freeCastFilter,
                duringTurnsLoreCounterPutOnSource, library, castOnly, true, duration, faceDown, forLife,
                player, permissionCondition, castFilter, playLimit, chooseOne);
    }

    /** The permission lasts for {@code duration} instead of until end of turn ("until your next end step"). */
    public ExileTopCardMayPlayThisTurnEffect until(Duration duration) {
        return new ExileTopCardMayPlayThisTurnEffect(count, withoutPayingManaCost, freeCastFilter,
                duringTurnsLoreCounterPutOnSource, library, castOnly, anyManaType, duration, faceDown, forLife,
                player, permissionCondition, castFilter, playLimit, chooseOne);
    }

    /** "Exile ... face down. You may look at and play that card." */
    public ExileTopCardMayPlayThisTurnEffect exiledFaceDown() {
        return new ExileTopCardMayPlayThisTurnEffect(count, withoutPayingManaCost, freeCastFilter,
                duringTurnsLoreCounterPutOnSource, library, castOnly, anyManaType, duration, true, forLife,
                player, permissionCondition, castFilter, playLimit, chooseOne);
    }

    /** "If you cast a spell this way, pay life equal to its mana value rather than pay its mana cost." */
    public ExileTopCardMayPlayThisTurnEffect payingLifeEqualToManaValue() {
        return new ExileTopCardMayPlayThisTurnEffect(count, withoutPayingManaCost, freeCastFilter,
                duringTurnsLoreCounterPutOnSource, library, castOnly, anyManaType, duration, faceDown, true,
                player, permissionCondition, castFilter, playLimit, chooseOne);
    }

    /** The cards may be played by {@code player} instead of the controller ("they may play those cards"). */
    public ExileTopCardMayPlayThisTurnEffect playableBy(PlayerSubject player) {
        return new ExileTopCardMayPlayThisTurnEffect(count, withoutPayingManaCost, freeCastFilter,
                duringTurnsLoreCounterPutOnSource, library, castOnly, anyManaType, duration, faceDown, forLife, player,
                permissionCondition, castFilter, playLimit, chooseOne);
    }

    /** The cards may be played only while {@code condition} holds ("during any turn you attacked with a token"). */
    public ExileTopCardMayPlayThisTurnEffect playableOnlyIf(Condition condition) {
        return new ExileTopCardMayPlayThisTurnEffect(count, withoutPayingManaCost, freeCastFilter,
                duringTurnsLoreCounterPutOnSource, library, castOnly, anyManaType, duration, faceDown, forLife, player,
                condition, castFilter, playLimit, chooseOne);
    }

    /**
     * "You may cast [filter] spells from among them": only a card castable as a matching spell gets the permission,
     * which casts only matching spells.
     */
    public ExileTopCardMayPlayThisTurnEffect mayOnlyCast(CardPredicate filter) {
        return new ExileTopCardMayPlayThisTurnEffect(count, withoutPayingManaCost, freeCastFilter,
                duringTurnsLoreCounterPutOnSource, library, true, anyManaType, duration, faceDown, forLife, player,
                permissionCondition, filter, playLimit, chooseOne);
    }

    /** "You may play one of those cards", "up to two of those cards": at most {@code limit} of them may be played. */
    public ExileTopCardMayPlayThisTurnEffect upTo(int limit) {
        return new ExileTopCardMayPlayThisTurnEffect(count, withoutPayingManaCost, freeCastFilter,
                duringTurnsLoreCounterPutOnSource, library, castOnly, anyManaType, duration, faceDown, forLife, player,
                permissionCondition, castFilter, limit, chooseOne);
    }

    /** "Choose one of them. You may play that card": the permission goes only to the card chosen as it resolves. */
    public ExileTopCardMayPlayThisTurnEffect choosingOne() {
        return new ExileTopCardMayPlayThisTurnEffect(count, withoutPayingManaCost, freeCastFilter,
                duringTurnsLoreCounterPutOnSource, library, castOnly, anyManaType, duration, faceDown, forLife, player,
                permissionCondition, castFilter, playLimit, true);
    }

    /**
     * A target library is a harmful player target ("exile the top card of target opponent's library"), a benign one
     * when that player gets the permission ("target player exiles ... they may play that card").
     */
    @Override
    public TargetSpec targetSpec() {
        if (!library.declaresTarget()) {
            return TargetSpec.NONE;
        }
        return player instanceof PlayerSubject.TargetPlayer
                ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.harmful(TargetPredicates.player());
    }
}
