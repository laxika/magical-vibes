package com.github.laxika.magicalvibes.model.effect;

/**
 * One-shot effect creating a delayed triggered ability: for the rest of the turn, whenever the
 * controller gains life, each of their opponents loses that much life. Adds a
 * {@code LifeGainOpponentLifeLossWatcher} to {@code GameData.lifeGainOpponentLifeLossWatchers}
 * (cleared at turn cleanup); {@code TriggerCollectionService.checkLifeGainTriggers} pushes one
 * life-loss trigger per watcher whose controller is the player who gained life. Multiple activations
 * stack. Used by Vizkopa Guildmage.
 */
public record RegisterLifeGainOpponentLifeLossThisTurnEffect() implements CardEffect {
}
