package com.github.laxika.magicalvibes.model.effect;

/**
 * Moves the source Aura onto a target creature (CR 303.4 attach). The Aura is expected to already be
 * on the battlefield; resolving reattaches it to {@code targetId} and gives it a new timestamp.
 * Reads {@code targetId} as the creature to attach to and {@code sourcePermanentId} as the Aura.
 * Used by Prison Term ("you may attach this Aura to that creature").
 */
public record AttachSourceAuraToTargetCreatureEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PERMANENT);
    }
}
