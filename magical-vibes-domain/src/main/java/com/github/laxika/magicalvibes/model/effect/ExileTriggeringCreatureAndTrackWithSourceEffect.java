package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Death-trigger effect for Colfenor's Urn: exile the dying creature card from the
 * graveyard it went to and track it as "exiled with" the source permanent (via
 * {@code GameData.exiledCards} by source permanent ID), so the source can later
 * count and return those cards.
 *
 * @param dyingCardId the card ID of the dying creature (null in the card definition,
 *                    filled at trigger time by the death-trigger collector)
 */
public record ExileTriggeringCreatureAndTrackWithSourceEffect(UUID dyingCardId) implements CardEffect {

    public ExileTriggeringCreatureAndTrackWithSourceEffect() {
        this(null);
    }
}
