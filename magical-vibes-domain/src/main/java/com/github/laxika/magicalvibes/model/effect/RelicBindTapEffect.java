package com.github.laxika.magicalvibes.model.effect;

/**
 * Relic Bind's modal triggered ability: "Whenever enchanted artifact becomes tapped, choose one —
 * this Aura deals 1 damage to target player or planeswalker; or target player gains 1 life."
 * <p>
 * Placed in the {@code ON_ENCHANTED_PERMANENT_TAPPED} slot and resolved by
 * {@code RelicBindTapEffectHandler}, which — at resolution — first prompts the controller to choose
 * a mode (a {@code ColorChoice}/list pick driven by {@code ChoiceContext.RelicBindModeChoice}) and
 * then routes the chosen mode's targeted effect through the shared {@code MayAbilityTriggerTarget}
 * target-selection flow. The engine has no cast-time modal machinery for triggered abilities, so the
 * mode and target are chosen as the ability resolves (functionally equivalent for this ability).
 */
public record RelicBindTapEffect() implements CardEffect {
}
