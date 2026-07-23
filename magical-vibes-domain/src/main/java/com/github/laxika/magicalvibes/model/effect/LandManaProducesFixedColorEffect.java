package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * Capability marker for Infernal Darkness-style global mana replacement: while any permanent with
 * this effect is on the battlefield, if a land is tapped for mana it produces {@link #color()}
 * instead of any other type (amount unchanged).
 *
 * <p>Detected via {@code instanceof LandManaProducesFixedColorEffect} in
 * {@code GameQueryService} (capability interface — not a concrete-effect dispatch).
 */
public interface LandManaProducesFixedColorEffect extends CardEffect {

    ManaColor color();
}
