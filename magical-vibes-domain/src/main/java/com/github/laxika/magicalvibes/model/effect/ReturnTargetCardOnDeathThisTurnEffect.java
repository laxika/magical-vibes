package com.github.laxika.magicalvibes.model.effect;

/**
 * Registers a delayed trigger: when the targeted creature dies this turn, return that card to the
 * battlefield under its owner's control.
 * <p>
 * At resolution, this effect reads the target permanent from the stack entry and records the
 * creature's card ID in {@code GameData.creaturesReturnedToBattlefieldOnDeathThisTurn}. When that
 * creature dies later in the same turn, the death pipeline pushes a triggered ability that returns
 * the card from its owner's graveyard to the battlefield. Used by Graceful Reprieve.
 */
public record ReturnTargetCardOnDeathThisTurnEffect() implements CardEffect {
}
