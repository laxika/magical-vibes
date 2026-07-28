package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;

import java.util.Map;

/**
 * Capability marker for Reality Twist-style global mana replacement: while any permanent with this
 * effect is on the battlefield, lands with basic land types produce the remapped color given by
 * {@link #landColorMapping()} when tapped for mana, instead of any other type.
 *
 * <p>Detected via {@code instanceof TwistBasicLandManaColorsEffect} in
 * {@code GameQueryService} (capability interface — not a concrete-effect dispatch).
 */
public interface TwistBasicLandManaColorsEffect extends CardEffect {

    /**
     * Basic land type to the color that type produces while this effect is active. Types absent
     * from the map are unaffected by this effect.
     */
    Map<CardSubtype, ManaColor> landColorMapping();
}
