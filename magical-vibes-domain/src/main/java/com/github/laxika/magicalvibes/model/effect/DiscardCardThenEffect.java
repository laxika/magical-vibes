package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.CardType;

/**
 * "Discard a [card matching filter]. If you do, [thenEffect]."
 *
 * <p>At resolution the controller discards a card from their hand that matches {@code filter}.
 * If (and only if) a card is actually discarded, {@code thenEffect} is pushed onto the stack as a
 * reflexive triggered ability. If the hand has no matching card, nothing happens. Wrap in
 * {@link MayEffect} for the optional "you may discard" case (Pack Guardian).
 *
 * @param filter           predicate to match discardable hand cards
 * @param thenEffect       effect to execute after a successful discard
 * @param cardDescription  human-readable description of what is discarded (e.g. "a land card")
 * @param condition        optional predicate tested against the discarded card before executing
 *                         {@code thenEffect}
 * @param useEntryTarget   when true, the entry's pre-bound target is carried through the discard
 *                         choice to the reflexive effect instead of asking for a new target
 * @param alternateCardType if non-null, use {@code alternateThenEffect} when the discarded card
 *                          has this type
 * @param alternateThenEffect alternate reflexive effect selected by {@code alternateCardType}
 * @param player            who must discard ({@code you()} by default); never a declared target: the
 *                          player is read from the entry ({@code triggeringPlayer()},
 *                          {@code targetPlayerDeclaredElsewhere()}, ...)
 * @param resolveThenDuringSameResolution continue resolving the current spell or ability after the discard
 *                          instead of creating a trigger
 * @param thenWithoutDiscard when true, {@code thenEffect} still resolves when there is no card to
 *                          discard ("Discard a card, then draw a card" is not an "if you do" clause)
 */
public record DiscardCardThenEffect(
        CardPredicate filter,
        CardEffect thenEffect,
        String cardDescription,
        CardPredicate condition,
        boolean useEntryTarget,
        CardType alternateCardType,
        CardEffect alternateThenEffect,
        PlayerSubject player,
        boolean resolveThenDuringSameResolution,
        boolean thenWithoutDiscard
) implements CardEffect {

    public DiscardCardThenEffect {
        Objects.requireNonNull(player, "player");
        if (player.declaresTarget()) {
            throw new IllegalArgumentException("A discard-then effect declares no player target: " + player);
        }
    }

    public DiscardCardThenEffect(CardPredicate filter, CardEffect thenEffect, String cardDescription,
                                 CardPredicate condition, boolean useEntryTarget,
                                 CardType alternateCardType, CardEffect alternateThenEffect,
                                 PlayerSubject player, boolean resolveThenDuringSameResolution) {
        this(filter, thenEffect, cardDescription, condition, useEntryTarget, alternateCardType,
                alternateThenEffect, player, resolveThenDuringSameResolution, false);
    }

    public DiscardCardThenEffect(CardPredicate filter, CardEffect thenEffect, String cardDescription,
                                 CardPredicate condition, boolean useEntryTarget,
                                 CardType alternateCardType, CardEffect alternateThenEffect,
                                 PlayerSubject player) {
        this(filter, thenEffect, cardDescription, condition, useEntryTarget, alternateCardType,
                alternateThenEffect, player, false);
    }

    /** Continues resolving the current spell after the discard instead of creating a trigger. */
    public static DiscardCardThenEffect continuing(CardPredicate filter, CardEffect thenEffect,
                                                   String cardDescription) {
        return new DiscardCardThenEffect(filter, thenEffect, cardDescription, null, false,
                null, null, Subjects.you(), true);
    }

    public DiscardCardThenEffect(CardPredicate filter, CardEffect thenEffect, String cardDescription) {
        this(filter, thenEffect, cardDescription, null, false, null, null, Subjects.you());
    }

    public DiscardCardThenEffect(CardPredicate filter, CardEffect thenEffect, String cardDescription,
                                 CardPredicate condition) {
        this(filter, thenEffect, cardDescription, condition, false, null, null,
                Subjects.you());
    }

    public DiscardCardThenEffect(CardPredicate filter, CardEffect thenEffect, String cardDescription,
                                 boolean useEntryTarget) {
        this(filter, thenEffect, cardDescription, null, useEntryTarget, null, null,
                Subjects.you());
    }

    public DiscardCardThenEffect(CardPredicate filter, CardEffect thenEffect, String cardDescription,
                                 CardType alternateCardType, CardEffect alternateThenEffect) {
        this(filter, thenEffect, cardDescription, null, false, alternateCardType, alternateThenEffect,
                Subjects.you());
    }

    public DiscardCardThenEffect(CardPredicate filter, CardEffect thenEffect, String cardDescription,
                                 CardPredicate condition, boolean useEntryTarget,
                                 CardType alternateCardType, CardEffect alternateThenEffect) {
        this(filter, thenEffect, cardDescription, condition, useEntryTarget, alternateCardType,
                alternateThenEffect, Subjects.you());
    }

    public DiscardCardThenEffect(CardPredicate filter, CardEffect thenEffect, String cardDescription,
                                 PlayerSubject player) {
        this(filter, thenEffect, cardDescription, null, false, null, null, player);
    }

    public DiscardCardThenEffect(CardPredicate filter, CardEffect thenEffect, String cardDescription,
                                 PlayerSubject player, boolean useEntryTarget) {
        this(filter, thenEffect, cardDescription, null, useEntryTarget, null, null, player);
    }

    @Override
    public TargetSpec targetSpec() {
        return useEntryTarget && thenEffect != null ? thenEffect.targetSpec() : TargetSpec.NONE;
    }
}
