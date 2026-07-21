package com.github.laxika.magicalvibes.model.effect;

/**
 * Hullbreaker Horror's modal spell-cast trigger: "Whenever you cast a spell, choose up to one —
 * return target spell you don't control to its owner's hand; or return target nonland permanent to
 * its owner's hand."
 * <p>
 * Placed via {@link SpellCastTriggerEffect} in {@code ON_CONTROLLER_CASTS_SPELL}. The engine has no
 * cast-time modal machinery for triggered abilities, so mode (+ target) are chosen as the ability
 * resolves (same pattern as {@link RelicBindTapEffect}). A "do nothing" mode covers "up to one".
 */
public record HullbreakerHorrorTriggerEffect() implements CardEffect {
}
