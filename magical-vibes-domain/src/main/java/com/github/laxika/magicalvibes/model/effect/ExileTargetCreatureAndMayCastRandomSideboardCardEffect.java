package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles target creature, then offers its controller a random nonland sideboard card to cast
 * without paying its mana cost.
 */
public record ExileTargetCreatureAndMayCastRandomSideboardCardEffect() implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.EXILE;
    }
}
