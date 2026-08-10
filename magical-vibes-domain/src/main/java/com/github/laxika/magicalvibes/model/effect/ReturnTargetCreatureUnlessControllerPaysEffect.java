package com.github.laxika.magicalvibes.model.effect;

/**
 * Returns the target creature to its owner's hand unless its controller pays the configured mana
 * cost. The decision belongs to the target creature's controller.
 */
public record ReturnTargetCreatureUnlessControllerPaysEffect(String manaCost) implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.BOUNCE;
    }
}
