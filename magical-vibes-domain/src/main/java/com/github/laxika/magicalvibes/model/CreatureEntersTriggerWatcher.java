package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.UUID;

/**
 * One active "whenever a creature you control enters this turn" delayed triggered ability.
 *
 * @param controllerId the player who controls the delayed ability
 * @param sourceCard the card that created the delayed ability
 * @param effect the effect resolved for each creature that enters
 */
public record CreatureEntersTriggerWatcher(UUID controllerId, Card sourceCard, CardEffect effect) {
}
