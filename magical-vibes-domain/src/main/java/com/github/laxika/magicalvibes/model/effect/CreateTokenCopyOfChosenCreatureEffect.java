package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Creates token copies of a creature chosen from among all creatures on the battlefield. */
public record CreateTokenCopyOfChosenCreatureEffect(int amount, UUID excludedPermanentId)
        implements CardEffect {
}
