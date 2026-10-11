package com.github.laxika.magicalvibes.model.replacement;

import com.github.laxika.magicalvibes.model.ResolutionDamageEvent;

/**
 * Damage one step of a simultaneous damage event started, held until every step of that event has
 * resolved. The held damage is then combined per recipient and source and dealt as one event each,
 * so a replacement or prevention effect applies once to everything one source deals one recipient
 * at the same time, and no step's amount sees damage another step dealt.
 *
 * @param event     the damage event the step belongs to
 * @param stepIndex the step's index in its stack entry's effect list
 * @param damage    the damage before any replacement or prevention effect
 * @param exilesDamagedCreatures a creature dealt this damage is exiled instead of dying this turn
 */
public record PendingSimultaneousDamage(ResolutionDamageEvent event, int stepIndex, DamageEvent damage,
                                        boolean exilesDamagedCreatures) {

    public PendingSimultaneousDamage copyForSimulation() {
        return new PendingSimultaneousDamage(event, stepIndex, damage.copyForSimulation(), exilesDamagedCreatures);
    }
}
