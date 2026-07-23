package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

/**
 * Display-only source attribution for an effect granted to a permanent by a continuous effect.
 * The effect instance is kept internal to the engine and is never serialized.
 */
public record GrantedEffectAttribution(String sourceName, CardEffect effect) {
}
