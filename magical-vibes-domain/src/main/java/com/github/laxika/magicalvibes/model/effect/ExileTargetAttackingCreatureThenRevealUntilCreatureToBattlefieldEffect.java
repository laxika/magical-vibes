package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Exiles the targeted attacking creature, then reveals the controller's library until a creature
 * card is found and puts it onto the battlefield tapped and attacking.
 */
public record ExileTargetAttackingCreatureThenRevealUntilCreatureToBattlefieldEffect(
        PermanentPredicate targetFilter
) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature(), targetFilter);
    }
}
