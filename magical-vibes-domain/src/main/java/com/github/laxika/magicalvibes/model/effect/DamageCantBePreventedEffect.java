package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: damage can't be prevented by any means.
 * Used by Leyline of Punishment and similar effects (e.g. Everlasting Torment).
 * Prevention shields, protection from colors (damage portion only), and all other
 * damage prevention effects are bypassed.
 */
public record DamageCantBePreventedEffect() implements CardEffect {
}
