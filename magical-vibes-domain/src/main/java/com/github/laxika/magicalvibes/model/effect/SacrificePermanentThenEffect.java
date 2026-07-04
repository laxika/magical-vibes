package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "Sacrifice a [permanent matching filter]. If you do, [thenEffect]."
 *
 * <p>At resolution the controller must sacrifice a permanent they control that
 * matches {@code filter}. If (and only if) a permanent is actually sacrificed,
 * {@code thenEffect} is executed. If the controller has no matching permanent,
 * nothing happens. When {@code thenEffect} is {@code null} this is a bare
 * "sacrifice a permanent" with no follow-up (e.g. Tragedy Feaster's Infusion).
 *
 * @param filter                predicate to match sacrificeable permanents
 * @param thenEffect            effect to execute after a successful sacrifice, or {@code null} for none
 * @param permanentDescription  human-readable description of what is sacrificed (e.g. "a Mountain")
 */
public record SacrificePermanentThenEffect(
        PermanentPredicate filter,
        CardEffect thenEffect,
        String permanentDescription
) implements CardEffect {
}
