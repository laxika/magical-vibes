package com.github.laxika.magicalvibes.model.effect;

/**
 * Wraps a continuous effect of an instant or sorcery that functions while that spell is on the
 * stack (CR 113.6), such as Torrent of Lava's "As long as Torrent of Lava is on the stack, each
 * creature has ...". The layered pass and the static-bonus assembly read it only from instant and
 * sorcery spells on the stack, so the wrapped effect reuses the ordinary static effect handlers.
 */
public record WrappedStackStaticEffect(CardEffect wrapped) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }
}
