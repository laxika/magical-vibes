package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;

/**
 * "Target opponent exiles a creature they control." (Doomfall mode 1).
 *
 * <p>The exile analog of {@link TargetPlayerChoosesCreatureDestroyEffect}: the targeted player
 * chooses which eligible permanent to lose, but it is <em>exiled</em> rather than destroyed, so
 * regeneration and indestructible do not apply and no "dies" triggers fire. With 0 eligible
 * permanents nothing happens; with exactly 1 it is exiled automatically; with 2+ the choosing
 * player picks. When {@code greatestPowerOnly} is true, only creatures tied for greatest power
 * are eligible and the controller of the effect chooses among them. A non-null
 * {@code permanentFilter} supplies a resolution-time eligibility filter instead of the default
 * creature-only filter.
 */
public record TargetPlayerChoosesCreatureExileEffect(boolean greatestPowerOnly,
                                                     PermanentPredicate permanentFilter)
        implements CardEffect {
    public TargetPlayerChoosesCreatureExileEffect() {
        this(false, null);
    }

    public TargetPlayerChoosesCreatureExileEffect(boolean greatestPowerOnly) {
        this(greatestPowerOnly, null);
    }

    public TargetPlayerChoosesCreatureExileEffect(PermanentPredicate permanentFilter) {
        this(false, permanentFilter);
    }

    @Override public TargetSpec targetSpec() { return TargetSpec.benign(TargetPredicates.player()); }

    @Override public PlayerRelation targetPlayerRelation() { return PlayerRelation.OPPONENT; }
}
