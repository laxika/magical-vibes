package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles one of the targeted player's creatures or planeswalkers with the greatest mana value.
 * The targeted player chooses among tied permanents.
 */
public record TargetPlayerExilesGreatestManaValueCreatureOrPlaneswalkerEffect()
        implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.EXILE;
    }
}
