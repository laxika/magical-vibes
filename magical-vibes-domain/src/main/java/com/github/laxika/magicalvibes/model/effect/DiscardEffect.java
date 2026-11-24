package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Makes one or more players discard cards (CR 701.9a). A single record covers the whole discard family:
 * the {@link PlayerSubject} names who discards ({@code you()}, {@code targetPlayer()},
 * {@code triggeringPlayer()}, {@code eachPlayer()}, {@code controllerOf(targetSpell())}, ...),
 * {@code random} chooses between the player picking their discards and a random
 * discard, and the {@link DynamicAmount} count covers fixed counts ("discards two cards"), an X
 * value (Mind Shatter), or a source-relative count such as {@code CountersOnSource(CHARGE)}
 * (Shrine of Limitless Power). When {@code stopAfterDiscardingType} or
 * {@code stopAfterDiscardingPredicate} is non-null, the chosen discard flow may finish after a
 * matching card, while still allowing the player to choose the remaining cards up to {@code amount}
 * (Thirst for Knowledge and Thirst for Discovery).
 *
 * @param amount                  number of cards to discard
 * @param player                  who discards; only {@code targetPlayer()} declares a player target
 * @param random                  when {@code true} the discard is at random; when {@code false}
 *                                the discarding player chooses which cards to discard
 * @param stopAfterDiscardingType matching card type that makes the remaining discard optional;
 *                                {@code null} when using a predicate or for an ordinary discard
 * @param stopAfterDiscardingPredicate matching card predicate that makes the remaining discard
 *                                     optional; {@code null} when using a card type or for an
 *                                     ordinary discard
 * @param onlyCardsDrawnThisResolution when {@code true}, choices are restricted to cards drawn
 *                                earlier while resolving the same stack entry
 * @param filter                  "discard a land card", "discard a nonland card" ({@link #matching}): only cards
 *                                matching it may be chosen, and a player with fewer of them discards those they
 *                                have (CR 609.3); {@code null} for any card. Only a chosen discard by players the
 *                                effect names without targeting them takes one
 * @param amountPerPlayer         the amount is evaluated once for each discarding player, player-relative scopes
 *                                ({@code CountScope.CONTROLLER}) reading that player ({@link #eachEvaluatedFor}:
 *                                "each opponent with more than five cards in hand discards cards equal to the
 *                                difference"); otherwise once, for the effect's controller
 */
public record DiscardEffect(DynamicAmount amount, PlayerSubject player, boolean random,
                            CardType stopAfterDiscardingType, boolean onlyCardsDrawnThisResolution,
                            CardPredicate stopAfterDiscardingPredicate, CardPredicate filter,
                            boolean amountPerPlayer)
        implements CardEffect {

    public DiscardEffect(DynamicAmount amount, PlayerSubject player, boolean random,
                         CardType stopAfterDiscardingType, boolean onlyCardsDrawnThisResolution,
                         CardPredicate stopAfterDiscardingPredicate, CardPredicate filter) {
        this(amount, player, random, stopAfterDiscardingType, onlyCardsDrawnThisResolution,
                stopAfterDiscardingPredicate, filter, false);
    }

    public DiscardEffect(DynamicAmount amount, PlayerSubject player, boolean random,
                         CardType stopAfterDiscardingType, boolean onlyCardsDrawnThisResolution,
                         CardPredicate stopAfterDiscardingPredicate) {
        this(amount, player, random, stopAfterDiscardingType, onlyCardsDrawnThisResolution,
                stopAfterDiscardingPredicate, null);
    }

    public DiscardEffect(DynamicAmount amount, PlayerSubject player, boolean random) {
        this(amount, player, random, null, false, null);
    }

    public DiscardEffect(DynamicAmount amount, PlayerSubject player, boolean random,
                         CardType stopAfterDiscardingType) {
        this(amount, player, random, stopAfterDiscardingType, false, null);
    }

    public DiscardEffect(DynamicAmount amount, PlayerSubject player,
                         CardPredicate stopAfterDiscardingPredicate) {
        this(amount, player, false, null, false, stopAfterDiscardingPredicate);
    }

    /** Fixed count, chosen or random per {@code random}. */
    public DiscardEffect(int amount, PlayerSubject player, boolean random) {
        this(new Fixed(amount), player, random);
    }

    /** Chosen discard that may finish after a matching card. */
    public DiscardEffect(int amount, PlayerSubject player, CardType stopAfterDiscardingType) {
        this(new Fixed(amount), player, false, stopAfterDiscardingType);
    }

    /** Chosen discard that may finish after a matching card predicate. */
    public DiscardEffect(int amount, PlayerSubject player,
                         CardPredicate stopAfterDiscardingPredicate) {
        this(new Fixed(amount), player, stopAfterDiscardingPredicate);
    }

    /** Dynamic count, non-random (the discarding player chooses). */
    public DiscardEffect(DynamicAmount amount, PlayerSubject player) {
        this(amount, player, false);
    }

    /** Fixed count, non-random (the discarding player chooses). */
    public DiscardEffect(int amount, PlayerSubject player) {
        this(new Fixed(amount), player, false);
    }

    public DiscardEffect {
        Objects.requireNonNull(player, "player");
        if (filter != null && (random || player.declaresTarget() || onlyCardsDrawnThisResolution
                || stopAfterDiscardingType != null || stopAfterDiscardingPredicate != null)) {
            throw new IllegalArgumentException(
                    "A card filter is for a plain chosen discard by a player named without targeting: " + player);
        }
        if (amountPerPlayer && player.declaresTarget()) {
            throw new IllegalArgumentException("An amount per player is for players named without targeting: "
                    + player);
        }
    }

    /** "Discard [amount] [land | nonland | creature] card(s)": a chosen discard of cards matching {@code filter}. */
    public static DiscardEffect matching(int amount, PlayerSubject player, CardPredicate filter) {
        return new DiscardEffect(new Fixed(amount), player, false, null, false, null,
                Objects.requireNonNull(filter, "filter"));
    }

    /**
     * "Each opponent with more than N cards in hand discards cards equal to the difference": a chosen discard by each
     * of {@code players}, {@code amount} evaluated for that player ({@code Max(Fixed(0), Sum(CardsInHand(CONTROLLER),
     * Fixed(-N)))}); a player whose amount is 0 or less discards nothing. The players choose in APNAP order, then
     * discard at once (CR 101.4).
     */
    public static DiscardEffect eachEvaluatedFor(DynamicAmount amount, PlayerSubject players) {
        return new DiscardEffect(amount, players, false, null, false, null, null, true);
    }

    public static DiscardEffect cardsDrawnThisResolution(int amount, PlayerSubject player) {
        return new DiscardEffect(new Fixed(amount), player, false, null, true, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return player.declaresTarget()
                ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
