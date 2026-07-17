package com.github.laxika.magicalvibes.model.effect;

/**
 * Registers a delayed trigger: when the targeted creature dies this turn, the effect's controller
 * creates the wrapped {@code tokenEffect}'s token(s).
 * <p>
 * At resolution, this effect reads the target permanent from the stack entry and records a
 * registration (token template + controller + source card) in
 * {@code GameData.creatureCreatingTokenOnDeathThisTurn} keyed by the creature's card ID. When that
 * creature dies later in the same turn, the death pipeline pushes a triggered ability that resolves
 * the token effect under the recorded controller's control. Used by Skeletonize.
 *
 * @param tokenEffect the token(s) to create when the targeted creature dies this turn
 */
public record CreateTokenOnTargetDeathThisTurnEffect(CreateTokenEffect tokenEffect) implements CardEffect {
}
