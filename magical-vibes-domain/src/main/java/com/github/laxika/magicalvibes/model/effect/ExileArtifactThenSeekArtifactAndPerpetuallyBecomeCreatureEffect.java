package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/** Exiles a controlled or graveyard artifact, then seeks and perpetually modifies an artifact card. */
public record ExileArtifactThenSeekArtifactAndPerpetuallyBecomeCreatureEffect(
        int power, int toughness, CardSubtype subtype) implements CardEffect {
}
