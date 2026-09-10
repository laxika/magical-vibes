package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroys the targeted creature, then has that creature's controller amass Goblins equal to the
 * creature's last-known power. The effect controller draws a card when they controlled the target.
 */
public record DestroyTargetCreatureThenAmassGoblinsEffect() implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.DESTROY;
    }
}
