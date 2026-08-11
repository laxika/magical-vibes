package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.UUID;

/**
 * One active "whenever a creature dies this turn" delayed triggered ability.
 *
 * @param controllerId the player who controls the delayed ability
 * @param sourceCard the card that created the delayed ability
 * @param effect the effect resolved by each delayed trigger
 */
public record CreatureDeathTriggerWatcher(UUID controllerId, Card sourceCard, CardEffect effect) {
}
