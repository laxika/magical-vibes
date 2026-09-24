package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/** Resolution continuation for an Aura revealed by Glimpse of Tomorrow. */
public record MorphicTideAuraEffect(UUID ownerId, Card auraCard, boolean randomBottomOrder)
        implements CardEffect {
}
