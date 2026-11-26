package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

import java.util.Set;

/**
 * A creature gets +{@code powerBoost}/+{@code toughnessBoost} (sublayer 7c, CR 613.4c) and gains {@code keywords}
 * (layer 6), as a floating continuous effect on that permanent. How long it lasts is the floating effect's
 * {@code Duration}, not this record's.
 *
 * <p>It is the payload the boost handlers put on a permanent, never a card's effect: cards name
 * {@link BoostEffect#target} (or another boost effect), whose handlers put it on the permanent through
 * {@code GameData.addPowerToughnessBoost} with the stated duration ({@code Duration.PERMANENT} when none is stated,
 * CR 611.2a) and the amounts fixed as the effect began (CR 608.2h). {@code GameQueryService.assembleStaticBonus} reads
 * every such effect on the permanent; several add up.
 */
public record PowerToughnessBoostEffect(int powerBoost, int toughnessBoost, Set<Keyword> keywords) implements CardEffect {

    public PowerToughnessBoostEffect(int powerBoost, int toughnessBoost) {
        this(powerBoost, toughnessBoost, Set.of());
    }
}
