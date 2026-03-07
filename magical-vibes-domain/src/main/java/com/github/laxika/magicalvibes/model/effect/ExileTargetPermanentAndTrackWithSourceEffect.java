package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles a target permanent and tracks the exiled card with the source permanent
 * via {@code GameData.permanentExiledCards}. Used by cards like Karn Liberated
 * whose abilities refer to cards "exiled with" it.
 */
public record ExileTargetPermanentAndTrackWithSourceEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
