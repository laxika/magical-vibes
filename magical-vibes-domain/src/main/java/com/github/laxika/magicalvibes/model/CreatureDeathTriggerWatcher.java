package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.UUID;

/**
 * One active "whenever a creature dies this turn" delayed triggered ability.
 *
 * @param controllerId the player who controls the delayed ability
 * @param sourceCard the card that created the delayed ability
 * @param effect the effect resolved by each delayed trigger
 * @param sourcePermanentId the permanent that was the source of the ability that created the delayed ability, which is
 *                          its source too (CR 603.7e), recorded whatever the effect is; its triggers keep it as their
 *                          source permanent, so "other than this creature" is that object, never a later one of the
 *                          same card; null when it had none (a spell)
 * @param onlyOthers        whether it triggers only for other creatures ("another creature dies"): the death of its
 *                          source permanent doesn't trigger it
 */
public record CreatureDeathTriggerWatcher(UUID controllerId, Card sourceCard, CardEffect effect,
                                          UUID sourcePermanentId, boolean onlyOthers) {

    public CreatureDeathTriggerWatcher(UUID controllerId, Card sourceCard, CardEffect effect) {
        this(controllerId, sourceCard, effect, null, false);
    }
}
