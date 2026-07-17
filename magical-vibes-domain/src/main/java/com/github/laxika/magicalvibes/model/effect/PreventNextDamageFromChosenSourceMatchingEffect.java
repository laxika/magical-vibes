package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "The next time a {@code sourceLabel} source of your choice would deal damage to you this turn,
 * prevent that damage." The source is chosen on resolution from the permanents matching
 * {@code sourceFilter} (e.g. red permanents, or artifacts). One-shot: only the next damage event
 * from that source is prevented, then the shield is consumed. The Circle of Protection cycle
 * (colored variants and Circle of Protection: Artifacts).
 *
 * @param sourceFilter restricts which permanents are legal source choices
 * @param sourceLabel  human-readable label for the source restriction, used in the choice prompt
 */
public record PreventNextDamageFromChosenSourceMatchingEffect(PermanentPredicate sourceFilter,
                                                              String sourceLabel) implements CardEffect {
}
