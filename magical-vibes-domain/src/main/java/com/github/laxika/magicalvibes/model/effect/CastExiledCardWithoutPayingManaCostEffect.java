package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Internal follow-up that casts a previously exiled card without paying its mana cost after its
 * discard payment has been completed.
 */
public record CastExiledCardWithoutPayingManaCostEffect(UUID exiledCardId) implements CardEffect {
}
