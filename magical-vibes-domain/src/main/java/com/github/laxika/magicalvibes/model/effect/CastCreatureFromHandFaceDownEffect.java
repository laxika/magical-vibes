package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;

import java.util.Map;
import java.util.UUID;

/**
 * Illusionary Mask's activated ability: offers a creature card from hand that could have been
 * paid for by some or all of the mana spent on the ability's X cost, then casts the chosen card
 * face down without paying its mana cost.
 */
public record CastCreatureFromHandFaceDownEffect(Map<ManaColor, Integer> manaSpent, UUID offerGroupId)
        implements CardEffect {

    /** The printed activated-ability effect; resolution fills in the activation snapshot. */
    public CastCreatureFromHandFaceDownEffect() {
        this(Map.of(), null);
    }

    public CastCreatureFromHandFaceDownEffect {
        manaSpent = manaSpent == null ? Map.of() : Map.copyOf(manaSpent);
    }
}
