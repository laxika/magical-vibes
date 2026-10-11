package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * Life one lifelink source has earned its controller during the current resolution. Damage that one
 * source deals to several recipients while a spell or ability resolves is summed here and gained as
 * a single life-gain event once the resolution ends, so "whenever you gain life" triggers once per
 * source rather than once per recipient. Separate sources, or the same source gaining life for
 * different players, stay separate gains.
 *
 * @param sourceId           the damage source: its permanent while it has one, otherwise its card
 * @param playerId           the player who gains the life
 * @param amount             the summed damage dealt
 * @param sourceCard         the card dealing the damage, passed to life-gain triggers
 * @param sourceEntryType    the stack entry type of the damage, passed to life-gain triggers
 * @param sourceControllerId the controller of the spell or ability whose damage caused the gain
 */
public record PendingLifelinkGain(UUID sourceId, UUID playerId, int amount, Card sourceCard,
                                  StackEntryType sourceEntryType, UUID sourceControllerId) {

    public boolean isSameEvent(UUID otherSourceId, UUID otherPlayerId) {
        return sourceId.equals(otherSourceId) && playerId.equals(otherPlayerId);
    }

    public PendingLifelinkGain plus(int additionalAmount) {
        return new PendingLifelinkGain(sourceId, playerId, amount + additionalAmount, sourceCard,
                sourceEntryType, sourceControllerId);
    }
}
