package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.UUID;

/**
 * One active "whenever a creature dies this turn" delayed triggered ability.
 *
 * @param controllerId the player who controls the delayed ability
 * @param sourceCard the card that created the delayed ability
 * @param effect the effect resolved by each delayed trigger
 * @param sourcePermanentId the permanent that created the delayed ability when it only triggers for other
 *                          creatures ("another creature dies"), or null when every death triggers it
 */
public record CreatureDeathTriggerWatcher(UUID controllerId, Card sourceCard, CardEffect effect,
                                          UUID sourcePermanentId) {

    public CreatureDeathTriggerWatcher(UUID controllerId, Card sourceCard, CardEffect effect) {
        this(controllerId, sourceCard, effect, null);
    }
}
