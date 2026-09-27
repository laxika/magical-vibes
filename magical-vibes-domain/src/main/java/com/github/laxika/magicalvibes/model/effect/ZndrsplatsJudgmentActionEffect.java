package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Resolves one creature choice made for Zndrsplt's Judgment. */
public record ZndrsplatsJudgmentActionEffect(UUID playerId, UUID creatureId, boolean friend)
        implements CardEffect {
}
