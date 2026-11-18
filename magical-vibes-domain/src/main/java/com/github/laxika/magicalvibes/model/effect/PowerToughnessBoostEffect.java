package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

import java.util.Set;

/**
 * A creature gets +{@code powerBoost}/+{@code toughnessBoost} (sublayer 7c, CR 613.4c) and gains {@code keywords}
 * (layer 6), as a floating continuous effect on that permanent. How long it lasts is the floating effect's
 * {@code Duration}, not this record's.
 *
 * <p>Named on a card it is "target creature gets +X/+Y and gains [keywords]" with no stated duration (Riding the
 * Dilu Horse's "This effect lasts indefinitely", Xathrid Gorgon's defender): its handler records it with
 * {@code Duration.PERMANENT} (CR 611.2a). For "until end of turn" and the other stated durations cards name
 * {@link BoostTargetCreatureEffect} (or another boost effect) instead, whose handlers put this payload on the permanent
 * through {@code GameData.addPowerToughnessBoost} with that duration and the amounts fixed as the effect began
 * (CR 608.2h). {@code GameQueryService.assembleStaticBonus} reads every such effect on the permanent; several add up.
 */
public record PowerToughnessBoostEffect(int powerBoost, int toughnessBoost, Set<Keyword> keywords) implements CardEffect {

    public PowerToughnessBoostEffect(int powerBoost, int toughnessBoost) {
        this(powerBoost, toughnessBoost, Set.of());
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
