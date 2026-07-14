package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * The {@link MillRecipient} mills {@code count} cards. The amount is a {@link DynamicAmount}, so the
 * same effect covers a fixed count ("mills three cards"), an X value ({@code XValue}), a derived
 * count such as charge counters on the source ({@code CountersOnSource(CHARGE)}), or the number of
 * cards in the target player's hand ({@code CardsInHand(TARGET_PLAYER)} for Dreamborn Muse).
 */
public record MillEffect(DynamicAmount count, MillRecipient recipient)
        implements CombatDamageTriggerContextEffect {

    /** Convenience constructor for a fixed mill count. */
    public MillEffect(int count, MillRecipient recipient) {
        this(new Fixed(count), recipient);
    }

    @Override
    public boolean canTargetPlayer() {
        return recipient == MillRecipient.TARGET_PLAYER;
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return recipient == MillRecipient.TARGET_PLAYER ? TriggerContext.DAMAGED_PLAYER : null;
    }
}
