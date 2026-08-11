package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Each permanent controlled by the effect's controller that matches {@code filter} becomes a
 * permanent copy of the targeted non-Aura permanent. The copy is permanent and does not copy
 * counters, attachments, or other non-copy modifications.
 */
public record EachControlledPermanentBecomesCopyOfTargetNonAuraPermanentEffect(
        PermanentPredicate filter) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent(),
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.AURA)));
    }
}
