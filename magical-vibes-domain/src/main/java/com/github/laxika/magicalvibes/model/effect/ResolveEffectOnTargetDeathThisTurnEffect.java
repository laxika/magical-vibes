package com.github.laxika.magicalvibes.model.effect;

/**
 * Registers a delayed trigger: when the targeted permanent dies this turn, the wrapped effect
 * resolves under the registering player's control.
 * <p>
 * At resolution, this effect reads the target permanent from the stack entry and records a
 * registration (wrapped effect + controller + source card + source permanent) in
 * {@code GameData.permanentTriggeringEffectOnDeathThisTurn} keyed by the permanent's card ID. When
 * that permanent dies later in the same turn, the death pipeline pushes a triggered ability that
 * resolves the wrapped effect, carrying the original source permanent so self-referential effects
 * (e.g. {@link TransformToBackFaceEffect}) still find it. Used by Skeletonize and Initiate of Blood.
 *
 * @param effect the effect to resolve when the targeted permanent dies this turn
 */
public record ResolveEffectOnTargetDeathThisTurnEffect(CardEffect effect) implements CardEffect {
}
