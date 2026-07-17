package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "Sacrifice {@code count} [permanents matching {@code filter}]. If you do, untap this creature."
 *
 * <p>All-or-nothing: at resolution the controller must control at least {@code count} matching
 * permanents. If they do, {@code count} of them are sacrificed and the source permanent is untapped;
 * if they control fewer than {@code count}, nothing happens (the "if you do" cost cannot be paid, so
 * the source is not untapped). Wrap in a {@link MayEffect} for the "you may" variant (Leviathan).
 *
 * @param count       number of matching permanents to sacrifice
 * @param filter      which permanents the controller may sacrifice
 * @param description human-readable description of the sacrifice (e.g. "two Islands")
 */
public record SacrificePermanentsToUntapSelfEffect(
        int count,
        PermanentPredicate filter,
        String description
) implements CardEffect {
}
