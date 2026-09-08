package com.github.laxika.magicalvibes.model.effect;

/**
 * Returns the target creature to its owner's hand, then creates tokens equal to that creature's
 * mana value under the resolving spell's controller's control.
 *
 * @param tokenTemplate token characteristics to use; its amount is replaced at resolution
 */
public record ReturnTargetCreatureToHandThenCreateTokensEqualToManaValueEffect(
        CreateTokenEffect tokenTemplate
) implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.BOUNCE;
    }
}
