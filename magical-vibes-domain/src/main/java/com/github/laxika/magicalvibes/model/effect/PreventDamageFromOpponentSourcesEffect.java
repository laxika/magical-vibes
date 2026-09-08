package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect that prevents damage from sources not controlled by this permanent's controller.
 *
 * <p>The fixed form prevents {@code amount} damage from each source. The unlimited form is used by
 * Energy Field, whose wording prevents all damage from those sources.
 *
 * @param amount the amount of damage to prevent per source
 */
public record PreventDamageFromOpponentSourcesEffect(int amount) implements CardEffect {

    public static PreventDamageFromOpponentSourcesEffect allDamage() {
        return new PreventDamageFromOpponentSourcesEffect(Integer.MAX_VALUE);
    }
}
