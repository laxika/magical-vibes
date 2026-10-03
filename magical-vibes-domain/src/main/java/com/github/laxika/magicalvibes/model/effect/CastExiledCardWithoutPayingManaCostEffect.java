package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Casts an exiled card without paying its mana cost during the resolution of a spell or ability.
 * {@code grantHaste} grants haste until end of turn; {@code suspendHaste} grants haste for as long
 * as the same player controls the resulting permanent.
 */
public record CastExiledCardWithoutPayingManaCostEffect(UUID exiledCardId, boolean grantHaste,
                                                        boolean suspendHaste) implements CardEffect {
    public CastExiledCardWithoutPayingManaCostEffect(UUID exiledCardId) {
        this(exiledCardId, false, false);
    }

    public CastExiledCardWithoutPayingManaCostEffect(UUID exiledCardId, boolean grantHaste) {
        this(exiledCardId, grantHaste, false);
    }
}
