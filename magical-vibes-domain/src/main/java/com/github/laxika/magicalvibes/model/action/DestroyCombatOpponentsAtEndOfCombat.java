package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/**
 * Creature whose combat opponents are destroyed when combat ends. The opponent set is resolved at
 * drain time, so blocks declared after the spell resolved are included. The boolean selects between
 * the bidirectional history and the directional set of creatures blocked by the target. Drained in
 * {@code CombatService.processEndOfCombatCombatOpponentDestructions()}.
 */
public record DestroyCombatOpponentsAtEndOfCombat(UUID creatureId, boolean onlyCreaturesBlockedByTarget)
        implements DelayedAction {

    public DestroyCombatOpponentsAtEndOfCombat(UUID creatureId) {
        this(creatureId, false);
    }
}
