package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;

/**
 * Removes a blocking creature from combat, updates the attackers it was blocking, and lets its
 * controller choose a legal attacking creature for it to block again.
 */
public record BalduvianWarlordEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature(), new PermanentIsBlockingPredicate());
    }
}
