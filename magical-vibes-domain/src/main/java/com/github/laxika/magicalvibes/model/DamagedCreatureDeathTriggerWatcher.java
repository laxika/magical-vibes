package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.UUID;

/**
 * One active delayed trigger watching creatures damaged by a particular source object.
 *
 * @param damageSourceId source object id used by this turn's damage history
 * @param controllerId player who controls the delayed ability
 * @param sourceCard card that created the delayed ability
 * @param effect effect resolved for each qualifying death
 */
public record DamagedCreatureDeathTriggerWatcher(
        UUID damageSourceId,
        UUID controllerId,
        Card sourceCard,
        CardEffect effect
) {
}
