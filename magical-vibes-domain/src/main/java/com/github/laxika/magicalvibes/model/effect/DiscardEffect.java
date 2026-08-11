package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Makes one or more players discard cards. A single record covers the whole discard family:
 * the {@link DiscardRecipient} routes who discards (controller / target player / each player /
 * each opponent), {@code random} chooses between the player picking their discards and a random
 * discard, and the {@link DynamicAmount} count covers fixed counts ("discards two cards"), an X
 * value (Mind Shatter), or a source-relative count such as {@code CountersOnSource(CHARGE)}
 * (Shrine of Limitless Power). When {@code stopAfterDiscardingType} is non-null, the chosen
 * discard flow may finish after a matching card, while still allowing the player to choose the
 * remaining cards up to {@code amount} (Thirst for Knowledge).
 *
 * @param amount                  number of cards to discard
 * @param recipient               who discards
 * @param random                  when {@code true} the discard is at random; when {@code false}
 *                                the discarding player chooses which cards to discard
 * @param stopAfterDiscardingType matching card type that makes the remaining discard optional;
 *                                {@code null} for an ordinary discard
 * @param onlyIfSacrificed       when {@code true}, an {@code ON_DEATH} effect only triggers when
 *                                its source was sacrificed
 */
public record DiscardEffect(DynamicAmount amount, DiscardRecipient recipient, boolean random,
                            CardType stopAfterDiscardingType, boolean onlyIfSacrificed)
        implements CombatDamageTriggerContextEffect {

    public DiscardEffect(DynamicAmount amount, DiscardRecipient recipient, boolean random) {
        this(amount, recipient, random, null, false);
    }

    public DiscardEffect(DynamicAmount amount, DiscardRecipient recipient, boolean random,
                         CardType stopAfterDiscardingType) {
        this(amount, recipient, random, stopAfterDiscardingType, false);
    }

    /** Fixed count, chosen or random per {@code random}. */
    public DiscardEffect(int amount, DiscardRecipient recipient, boolean random) {
        this(new Fixed(amount), recipient, random);
    }

    /** Chosen discard that may finish after a matching card. */
    public DiscardEffect(int amount, DiscardRecipient recipient, CardType stopAfterDiscardingType) {
        this(new Fixed(amount), recipient, false, stopAfterDiscardingType);
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
        return new DiscardEffect(new Fixed(amount), DiscardRecipient.TARGET_PLAYER, false, null, true);
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
