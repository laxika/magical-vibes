package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "You may sacrifice a [permanent matching {@code filter}]. If you do, put a +1/+1 counter on this
 * creature. If you don't, tap this creature." (Ravenous Vampire)
 *
 * <p>At resolution the controller is asked whether to sacrifice; accepting sacrifices one matching
 * permanent they control (choosing which when several qualify) and puts a +1/+1 counter on the
 * source. Declining — or controlling nothing that matches, in which case no prompt is shown — taps
 * the source instead. The source itself is only a legal sacrifice when {@code filter} matches it.
 *
 * @param filter      the permanents that may be sacrificed
 * @param description human-readable description used in the prompts (e.g. "a nonartifact creature")
 */
public record MaySacrificePermanentForCounterOrTapSourceEffect(
        PermanentPredicate filter,
        String description
) implements CardEffect {
}
