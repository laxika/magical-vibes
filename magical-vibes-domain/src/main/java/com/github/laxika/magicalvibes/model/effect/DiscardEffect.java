package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Makes one or more players discard cards. A single record covers the whole discard family:
 * the {@link DiscardRecipient} routes who discards (controller / target player / triggering player /
 * each player /
 * each opponent), {@code random} chooses between the player picking their discards and a random
 * discard, and the {@link DynamicAmount} count covers fixed counts ("discards two cards"), an X
 * value (Mind Shatter), or a source-relative count such as {@code CountersOnSource(CHARGE)}
 * (Shrine of Limitless Power). When {@code stopAfterDiscardingType} or
 * {@code stopAfterDiscardingPredicate} is non-null, the chosen discard flow may finish after a
 * matching card, while still allowing the player to choose the remaining cards up to {@code amount}
 * (Thirst for Knowledge and Thirst for Discovery).
 *
 * @param amount                  number of cards to discard
 * @param recipient               who discards
 * @param random                  when {@code true} the discard is at random; when {@code false}
 *                                the discarding player chooses which cards to discard
 * @param stopAfterDiscardingType matching card type that makes the remaining discard optional;
 *                                {@code null} when using a predicate or for an ordinary discard
 * @param stopAfterDiscardingPredicate matching card predicate that makes the remaining discard
 *                                     optional; {@code null} when using a card type or for an
 *                                     ordinary discard
 * @param onlyIfSacrificed       when {@code true}, an {@code ON_DEATH} effect only triggers when
 *                                its source was sacrificed
 * @param onlyCardsDrawnThisResolution when {@code true}, choices are restricted to cards drawn
 *                                earlier while resolving the same stack entry
 */
public record DiscardEffect(DynamicAmount amount, DiscardRecipient recipient, boolean random,
                            CardType stopAfterDiscardingType, boolean onlyIfSacrificed,
                            boolean onlyCardsDrawnThisResolution,
                            CardPredicate stopAfterDiscardingPredicate)
        implements CombatDamageTriggerContextEffect {

    public DiscardEffect(DynamicAmount amount, DiscardRecipient recipient, boolean random) {
        this(amount, recipient, random, null, false, false, null);
    }

    public DiscardEffect(DynamicAmount amount, DiscardRecipient recipient, boolean random,
                         CardType stopAfterDiscardingType) {
        this(amount, recipient, random, stopAfterDiscardingType, false, false, null);
    }

    public DiscardEffect(DynamicAmount amount, DiscardRecipient recipient,
                         CardPredicate stopAfterDiscardingPredicate) {
        this(amount, recipient, false, null, false, false, stopAfterDiscardingPredicate);
    }

    /** Fixed count, chosen or random per {@code random}. */
    public DiscardEffect(int amount, DiscardRecipient recipient, boolean random) {
        this(new Fixed(amount), recipient, random);
    }

    /** Chosen discard that may finish after a matching card. */
    public DiscardEffect(int amount, DiscardRecipient recipient, CardType stopAfterDiscardingType) {
        this(new Fixed(amount), recipient, false, stopAfterDiscardingType);
    }

    /** Chosen discard that may finish after a matching card predicate. */
    public DiscardEffect(int amount, DiscardRecipient recipient,
                         CardPredicate stopAfterDiscardingPredicate) {
        this(new Fixed(amount), recipient, stopAfterDiscardingPredicate);
    }

    /** Dynamic count, non-random (the discarding player chooses). */
    public DiscardEffect(DynamicAmount amount, DiscardRecipient recipient) {
        this(amount, recipient, false);
    }

    /** Fixed count, non-random (the discarding player chooses). */
    public DiscardEffect(int amount, DiscardRecipient recipient) {
        this(new Fixed(amount), recipient, false);
    }

    public static DiscardEffect sacrificeOnly(int amount) {
        return new DiscardEffect(new Fixed(amount), DiscardRecipient.TARGET_PLAYER,
                false, null, true, false, null);
    }

    public static DiscardEffect cardsDrawnThisResolution(int amount, DiscardRecipient recipient) {
        return new DiscardEffect(new Fixed(amount), recipient, false, null, false, true, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return recipient == DiscardRecipient.TARGET_PLAYER
                ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return recipient == DiscardRecipient.TARGET_PLAYER ? TriggerContext.DAMAGED_PLAYER : null;
    }

    @Override
    public boolean onlyTriggersOnSacrifice() {
        return onlyIfSacrificed;
    }
}
