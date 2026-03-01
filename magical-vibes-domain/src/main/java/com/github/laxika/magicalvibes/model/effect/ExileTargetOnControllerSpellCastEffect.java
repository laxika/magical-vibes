package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker effect stored in an Emblem's staticEffects list.
 * Indicates that whenever the emblem's controller casts a spell,
 * a triggered ability fires that exiles target permanent.
 */
public record ExileTargetOnControllerSpellCastEffect() implements CardEffect {
    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
