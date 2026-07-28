package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

import java.util.Set;

/**
 * Ghostly Flame: "Black and/or red permanents and spells are colorless sources of damage."
 * A static marker; while a permanent with this effect is on the battlefield, a damage source
 * whose colour is among {@link #colors()} is treated as colourless <em>for damage purposes only</em>.
 * Protection from that colour therefore no longer prevents its damage (though it still stops
 * blocking, targeting, and enchanting), and colour-based damage prevention (Circle of Protection,
 * Prismatic Ward, "prevent all damage from red sources") no longer applies to it.
 *
 * <p>Queried through {@code GameQueryService.getDamageSourceColor} /
 * {@code hasProtectionFromDamageSource} from the damage paths.
 */
public record DamageSourcesOfColorsAreColorlessEffect(Set<CardColor> colors) implements CardEffect {
}
