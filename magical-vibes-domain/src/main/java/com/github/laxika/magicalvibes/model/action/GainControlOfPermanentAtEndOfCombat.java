package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.effect.ControlDuration;

import java.util.UUID;

/**
 * Permanent scheduled to change control when combat ends (e.g. The Wretched's "At end of combat,
 * gain control of all creatures blocking this creature for as long as you control this creature").
 * {@code newControllerId} gains control of {@code permanentId} for {@code duration}, keyed to
 * {@code sourcePermanentId}. A source-linked duration (e.g. {@code WHILE_SOURCE_ON_BATTLEFIELD})
 * also requires the source to still be on the battlefield under {@code newControllerId} for control
 * to be taken at all; a {@code PERMANENT} gain (Tolarian Entrancer) happens regardless. Drained in
 * {@code CombatService.processEndOfCombatControlGains()}.
 */
public record GainControlOfPermanentAtEndOfCombat(
        UUID permanentId,
        UUID newControllerId,
        UUID sourcePermanentId,
        String sourceCardName,
        ControlDuration duration
) implements DelayedAction {
}
