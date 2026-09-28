package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/** Makes the card selected from a spellbook an artifact creature for the rest of the game. */
public record PerpetuallyMakeSelectedSpellbookCardArtifactCreatureEffect(List<UUID> candidateCardIds)
        implements CardEffect {

    public PerpetuallyMakeSelectedSpellbookCardArtifactCreatureEffect {
        candidateCardIds = List.copyOf(candidateCardIds);
    }
}
