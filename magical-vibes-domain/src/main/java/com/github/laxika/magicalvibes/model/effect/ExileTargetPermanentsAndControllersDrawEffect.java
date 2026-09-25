package com.github.laxika.magicalvibes.model.effect;

/** Exiles each chosen target permanent and has its controller draw a card. */
public record ExileTargetPermanentsAndControllersDrawEffect() implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.EXILE;
    }
}
