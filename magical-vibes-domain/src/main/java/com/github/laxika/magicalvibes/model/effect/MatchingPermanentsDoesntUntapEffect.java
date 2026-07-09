package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Global static effect: permanents matching {@code filter} don't untap during their controllers'
 * untap steps, no matter who controls the source. Read during each player's untap step by
 * {@code UntapStepService}, which scans every battlefield for a source carrying this effect and
 * skips untapping any of the active player's permanents that match the filter.
 *
 * <p>Unlike {@link DoesntUntapEffect#self()} / {@link DoesntUntapEffect#enchanted()} (which lock a
 * single, specific permanent), this locks an open-ended set selected by a predicate across all
 * players — e.g. Marble Titan: "Creatures with power 3 or greater don't untap during their
 * controllers' untap steps" ({@code PermanentPowerAtLeastPredicate(3)}). Placed in
 * {@code EffectSlot.STATIC}.
 *
 * @param filter selects which permanents are prevented from untapping
 */
public record MatchingPermanentsDoesntUntapEffect(PermanentPredicate filter) implements CardEffect {
}
