package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Lets the controller of the targeted spell put a matching card from their hand onto the
 * battlefield. The targeted spell must still be on the stack when this effect resolves.
 */
public record PutCardToBattlefieldForTargetSpellControllerEffect(CardPredicate predicate, String label)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }
}
