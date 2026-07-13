package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Global static effect: while the source permanent is on the battlefield, any creature matching
 * {@code matcher} (on any battlefield) can't attack and can't block
 * (e.g. Light of Day: "Black creatures can't attack or block."). The attack side is evaluated in
 * {@code CombatAttackService.isCantAttackDueToGlobalRestriction}; the block side in
 * {@code GameQueryService.canBlock}.
 *
 * @param matcher     which creatures are prevented from attacking and blocking (the "Black creatures")
 * @param description short human-readable phrase for the restriction message
 */
public record MatchingCreaturesCantAttackOrBlockEffect(PermanentPredicate matcher,
                                                       String description) implements CardEffect {
}
