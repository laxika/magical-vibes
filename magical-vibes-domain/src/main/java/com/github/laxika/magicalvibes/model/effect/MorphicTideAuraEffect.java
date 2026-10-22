package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

import java.util.Set;
import java.util.UUID;

/**
 * Resolution continuation for an Aura revealed by Glimpse of Tomorrow.
 *
 * @param batchAuraCardIds every Aura card put onto the battlefield in the same batch; they enter at
 *                         the same time, so none of them can be chosen for another to enchant
 */
public record MorphicTideAuraEffect(UUID ownerId, Card auraCard, boolean randomBottomOrder,
                                    Set<UUID> batchAuraCardIds)
        implements CardEffect {
}
