package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: permanents matching {@code scope} become colorless (layer 5 color-setting,
 * CR 105.3 — replaces existing colors with an empty set). Used by Imprisoned in the Moon
 * ("Enchanted permanent is a colorless land …").
 *
 * @param scope which permanents are affected (typically {@link GrantScope#ENCHANTED_PERMANENT});
 *              {@link GrantScope#ALL_PERMANENTS} affects every other permanent and uses the
 *              self-handler to include the source permanent
 */
public record BecomeColorlessEffect(GrantScope scope) implements CardEffect {
}
