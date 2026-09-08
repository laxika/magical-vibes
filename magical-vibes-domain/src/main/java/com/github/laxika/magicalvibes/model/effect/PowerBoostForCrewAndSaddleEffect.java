package com.github.laxika.magicalvibes.model.effect;

/**
 * Static pilot-style power bonus used only when the source creature pays a crew or saddle cost.
 */
public record PowerBoostForCrewAndSaddleEffect(int powerBonus) implements CrewAndSaddlePowerModifierEffect {
}
