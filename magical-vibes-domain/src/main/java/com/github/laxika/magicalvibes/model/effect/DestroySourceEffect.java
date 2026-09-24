package com.github.laxika.magicalvibes.model.effect;

/** Destroys the source permanent immediately, respecting indestructible and regeneration. */
public record DestroySourceEffect() implements RemovalEffect {

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.DESTROY;
    }
}
