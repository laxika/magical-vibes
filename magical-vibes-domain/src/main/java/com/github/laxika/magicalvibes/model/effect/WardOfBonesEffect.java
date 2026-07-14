package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: each opponent who controls more creatures/artifacts/enchantments than the source's
 * controller can't cast spells of that type, and each opponent who controls more lands than the
 * controller can't play lands. Each permanent type is compared independently. Used by Ward of Bones
 * (EVE). Enforced in {@code CastingPermissionService}.
 */
public record WardOfBonesEffect() implements CardEffect {
}
