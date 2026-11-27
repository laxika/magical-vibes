package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the target permanent, then lets the effect controller cast that card until the end of
 * their next turn.
 *
 * @param manaAsThoughAnyColor "and you may spend mana as though it were mana of any color to cast that spell": a {C}
 *                             symbol is still paid only with colorless mana (CR 107.4c)
 */
public record ExileTargetPermanentAndGrantControllerCastPermissionUntilNextTurnEffect(
        boolean manaAsThoughAnyColor) implements RemovalEffect {

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
