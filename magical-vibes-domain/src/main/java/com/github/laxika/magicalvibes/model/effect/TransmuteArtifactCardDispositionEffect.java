package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Moves Transmute Artifact's selected card to the battlefield or its owner's graveyard. */
public record TransmuteArtifactCardDispositionEffect(UUID cardId, boolean toBattlefield)
        implements CardEffect {
}
