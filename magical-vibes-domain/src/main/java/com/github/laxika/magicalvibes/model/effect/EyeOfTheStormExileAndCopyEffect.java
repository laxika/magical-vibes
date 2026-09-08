package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Resolves Eye of the Storm's exile-and-copy triggered ability. */
public record EyeOfTheStormExileAndCopyEffect(
        UUID originalSpellCardId,
        UUID sourcePermanentId,
        UUID castingPlayerId
) implements CardEffect {
}
