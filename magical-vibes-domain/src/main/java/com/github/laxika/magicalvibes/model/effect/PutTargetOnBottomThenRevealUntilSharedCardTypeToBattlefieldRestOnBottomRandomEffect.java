package com.github.laxika.magicalvibes.model.effect;

/**
 * Puts the targeted permanent on the bottom of its owner's library, then reveals cards from the
 * controller's library until a permanent card sharing a card type with the moved permanent is
 * revealed. The matching card enters the battlefield and the other revealed cards go on the
 * bottom of that library in a random order.
 */
public record PutTargetOnBottomThenRevealUntilSharedCardTypeToBattlefieldRestOnBottomRandomEffect()
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
