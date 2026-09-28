package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the target permanent, then lets the effect controller cast that card until the end of
 * their next turn.
 *
 * @param anyManaType whether mana of any type may be spent to cast the exiled card
 */
public record ExileTargetPermanentAndGrantControllerCastPermissionUntilNextTurnEffect(
        boolean anyManaType) implements RemovalEffect {

    public ExileTargetPermanentAndGrantControllerCastPermissionUntilNextTurnEffect() {
        this(false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.EXILE;
    }
}
