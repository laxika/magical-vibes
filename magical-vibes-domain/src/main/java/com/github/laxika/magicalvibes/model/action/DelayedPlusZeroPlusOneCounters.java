package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/**
 * The prevention shield of Sacred Boon and Scars of the Veteran ("prevent the next N damage that would be dealt to
 * target creature this turn ... put a +0/+1 counter on it for each 1 damage prevented this way"): a prevention, not a
 * triggered ability, so it stays here with what it has prevented. The delayed triggered ability the spell made reads
 * the count as it resolves, so damage prevented in response to it is still counted.
 *
 * @param permanentId     the creature the shield protects
 * @param totalCounters   the damage the shield has prevented so far, which is the number of +0/+1 counters owed
 * @param shieldId        what the delayed triggered ability names the shield by
 * @param remainingShield the damage the shield can still prevent this turn
 * @param triggered       whether the delayed triggered ability has resolved; the shield, if it still prevents, then
 *                        ends at cleanup with nothing owed
 */
public record DelayedPlusZeroPlusOneCounters(UUID permanentId, int totalCounters, UUID shieldId, int remainingShield,
                                              boolean triggered) implements DelayedAction {

    public DelayedPlusZeroPlusOneCounters(UUID permanentId, int totalCounters, UUID shieldId, int remainingShield) {
        this(permanentId, totalCounters, shieldId, remainingShield, false);
    }
}
