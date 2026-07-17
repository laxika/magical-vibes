package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/**
 * Permanent scheduled to change control when combat ends (e.g. The Wretched's "At end of combat,
 * gain control of all creatures blocking this creature for as long as you control this creature").
 * {@code newControllerId} gains control of {@code permanentId}; control is applied as a
 * {@code WHILE_SOURCE_ON_BATTLEFIELD} effect keyed to {@code sourcePermanentId}, so it ends when the
 * source leaves the battlefield or its controller loses control of it. Drained in
 * {@code CombatService.processEndOfCombatControlGains()}.
 */
public record GainControlOfPermanentAtEndOfCombat(
        UUID permanentId,
        UUID newControllerId,
        UUID sourcePermanentId,
        String sourceCardName
) implements DelayedAction {
}
