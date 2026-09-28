package com.github.laxika.magicalvibes.model.effect;

/**
 * Perpetually reduces the generic cost of every card in the controller's hand that shares a card
 * type with the targeted spell.
 */
public record PerpetuallyReduceCostForHandCardsSharingTargetSpellEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }
}
