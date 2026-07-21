package com.github.laxika.magicalvibes.model.effect;

/**
 * Exile target creature. If that creature was a God, its controller reveals their hand and exiles
 * all cards from it with the same name as that creature. Used by Hour of Glory.
 */
public record ExileTargetCreatureAndSameNameFromHandIfGodEffect() implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.CREATURE);
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.EXILE;
    }
}
