package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker effect stored in an Emblem's staticEffects list.
 * Indicates that whenever the emblem's controller draws a card,
 * a triggered ability fires that exiles target permanent an opponent controls.
 */
public record ExileTargetOpponentPermanentOnDrawEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
