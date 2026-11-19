package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Only creatures matching {@code filter} can attack while this floating effect on every player lasts: the aggressive
 * combat phase, "only creatures with aggressive may attack during it", until that combat ends (CR 511.3).
 *
 * @param filter the creatures that can still attack, judged against each creature's current characteristics
 */
public record OnlyMatchingCreaturesCanAttackThisCombatEffect(PermanentPredicate filter) implements CardEffect {
}
