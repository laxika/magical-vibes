package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroys the targeted creature, then creates one token under the effect controller if the
 * creature was actually destroyed. The token's power and toughness are copied from the creature's
 * last-known effective power and toughness.
 *
 * @param tokenTemplate token characteristics to use, with power and toughness replaced at
 *                     resolution
 */
public record DestroyTargetCreatureThenCreateTokenEqualToPowerToughnessEffect(
        CreateTokenEffect tokenTemplate
) implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.DESTROY;
    }
}
