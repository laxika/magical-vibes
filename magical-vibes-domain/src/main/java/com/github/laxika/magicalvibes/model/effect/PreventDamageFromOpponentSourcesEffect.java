package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect that prevents damage from sources not controlled by this permanent's controller.
 *
 * <p>The fixed form prevents {@code amount} damage from each source. The unlimited form is used by
 * Energy Field, whose wording prevents all damage from those sources.
 *
 * <p>By default only damage to the controller is affected; {@code includesPermanents} also covers
 * damage to permanents the controller controls (Valkmira, Protector's Shield).
 *
 * @param amount             the amount of damage to prevent per source
 * @param includesPermanents whether damage to the controller's permanents is prevented as well
 */
public record PreventDamageFromOpponentSourcesEffect(int amount, boolean includesPermanents) implements CardEffect {

    public PreventDamageFromOpponentSourcesEffect(int amount) {
        this(amount, false);
    }

    public static PreventDamageFromOpponentSourcesEffect allDamage() {
        return new PreventDamageFromOpponentSourcesEffect(Integer.MAX_VALUE);
    }
}
