package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Replacement effect: if a permanent with at least one counter of {@code counterType} on it would
 * untap during its controller's untap step, all counters of that type are removed from it instead
 * (Freyalise's Winds — "If a permanent with a wind counter on it would untap during its controller's
 * untap step, remove all wind counters from it instead").
 *
 * <p>Placed in {@link com.github.laxika.magicalvibes.model.EffectSlot#STATIC}. It applies to every
 * permanent on every battlefield for as long as the source is on the battlefield, and is consulted
 * by {@code UntapStepService} at the point the permanent would untap. Because it only replaces the
 * untap step untap, other untap effects (Seedborn Muse untapping during another player's untap step,
 * "untap target permanent") are unaffected.</p>
 */
public record RemoveCountersInsteadOfUntappingEffect(CounterType counterType) implements CardEffect {
}
