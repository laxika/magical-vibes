package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Target creature gets +X/+Y until end of turn. The amounts are {@link DynamicAmount}s, so
 * "gets +3/+3", "gets +X/+X" (X paid), and "gets +1/+1 for each creature you control" are the
 * same effect with different amount parameters. Counting contexts ("you control", "in your
 * graveyard") resolve against the effect's controller, not the target.
 *
 * <p>{@code filter} is an OPTIONAL targeting restriction ({@code null} for a plain "target
 * creature"). When set it narrows the {@link #targetSpec()} predicate — e.g. Ominous Sphinx's
 * "target creature an opponent controls gets -2/-0" passes a "creature an opponent controls"
 * predicate. Trigger pipelines that read {@code targetSpec().predicate()} (such as the
 * cycle/discard controller-trigger target collector) use it to build the legal-target list.
 *
 * <p>{@code duration} is {@link GrantDuration#END_OF_TURN} for the overwhelming majority of pumps;
 * {@link GrantDuration#UNTIL_YOUR_NEXT_TURN} routes the modifier into the permanent's
 * until-next-turn bucket instead, so it survives end-of-turn cleanup (Jace, Telepath Unbound's +1).
 */
public record BoostTargetCreatureEffect(DynamicAmount powerBoost, DynamicAmount toughnessBoost,
                                        PermanentPredicate filter,
                                        GrantDuration duration) implements CreatureBoostEffect {

    public BoostTargetCreatureEffect(DynamicAmount powerBoost, DynamicAmount toughnessBoost,
                                     PermanentPredicate filter) {
        this(powerBoost, toughnessBoost, filter, GrantDuration.END_OF_TURN);
    }

    public BoostTargetCreatureEffect(DynamicAmount powerBoost, DynamicAmount toughnessBoost) {
        this(powerBoost, toughnessBoost, null);
    }

    /** Convenience for plain fixed boosts ("gets +2/+2 until end of turn"). */
    public BoostTargetCreatureEffect(int powerBoost, int toughnessBoost) {
        this(new Fixed(powerBoost), new Fixed(toughnessBoost), null);
    }

    /** Convenience for fixed boosts narrowed by a target restriction ("target creature an opponent controls gets -2/-0"). */
    public BoostTargetCreatureEffect(int powerBoost, int toughnessBoost, PermanentPredicate filter) {
        this(new Fixed(powerBoost), new Fixed(toughnessBoost), filter);
    }

    /** Convenience for fixed boosts with a non-default duration ("gets -2/-0 until your next turn"). */
    public BoostTargetCreatureEffect(int powerBoost, int toughnessBoost, GrantDuration duration) {
        this(new Fixed(powerBoost), new Fixed(toughnessBoost), null, duration);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature(), filter);
    }
}
