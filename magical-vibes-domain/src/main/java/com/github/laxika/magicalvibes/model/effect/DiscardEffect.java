package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Makes one or more players discard cards. A single record covers the whole discard family:
 * the {@link DiscardRecipient} routes who discards (controller / target player / each player /
 * each opponent), {@code random} chooses between the player picking their discards and a random
 * discard, and the {@link DynamicAmount} count covers fixed counts ("discards two cards"), an X
 * value (Mind Shatter), or a source-relative count such as {@code CountersOnSource(CHARGE)}
 * (Shrine of Limitless Power).
 *
 * @param amount    number of cards to discard
 * @param recipient who discards
 * @param random    when {@code true} the discard is at random; when {@code false} the discarding
 *                  player chooses which cards to discard
 */
public record DiscardEffect(DynamicAmount amount, DiscardRecipient recipient, boolean random)
        implements CombatDamageTriggerContextEffect {

    /** Fixed count, chosen or random per {@code random}. */
    public DiscardEffect(int amount, DiscardRecipient recipient, boolean random) {
        this(new Fixed(amount), recipient, random);
    }

    /** Dynamic count, non-random (the discarding player chooses). */
    public DiscardEffect(DynamicAmount amount, DiscardRecipient recipient) {
        this(amount, recipient, false);
    }

    /** Fixed count, non-random (the discarding player chooses). */
    public DiscardEffect(int amount, DiscardRecipient recipient) {
        this(new Fixed(amount), recipient, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return recipient == DiscardRecipient.TARGET_PLAYER
                ? TargetSpec.benign(TargetCategory.PLAYER) : TargetSpec.NONE;
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return recipient == DiscardRecipient.TARGET_PLAYER ? TriggerContext.DAMAGED_PLAYER : null;
    }
}
