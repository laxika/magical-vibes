package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "You may sacrifice exactly N matching permanents. If you do, [sacrificedEffect]. Otherwise,
 * [elseEffect]."
 *
 * <p>The controller chooses either no permanents or exactly {@code count} matching permanents at
 * resolution. If fewer than {@code count} matching permanents are available, the else effect is
 * resolved without opening a choice.</p>
 */
public record SacrificePermanentsOrElseEffect(
        PermanentPredicate filter,
        int count,
        CardEffect sacrificedEffect,
        CardEffect elseEffect,
        String permanentDescription
) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        TargetSpec sacrificedSpec = sacrificedEffect.targetSpec();
        return sacrificedSpec != TargetSpec.NONE ? sacrificedSpec : elseEffect.targetSpec();
    }
}
