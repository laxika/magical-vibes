package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Lets the controller cast up to {@code maxCount} nonland spells from their hand without paying
 * their mana costs.
 *
 * <p>The choice-group id is assigned when the effect resolves and is used to keep the individual
 * per-card may abilities together while the casts are being selected.
 */
public record CastUpToNSpellsFromHandWithoutPayingManaCostEffect(int maxCount,
                                                                  UUID choiceGroupId)
        implements CardEffect {

    public CastUpToNSpellsFromHandWithoutPayingManaCostEffect(int maxCount) {
        this(maxCount, null);
    }
}
