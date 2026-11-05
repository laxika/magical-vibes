package com.github.laxika.magicalvibes.model.effect;

/**
 * Death-trigger effect for Colfenor's Urn: exile the dying creature card from the
 * graveyard it went to and track it as "exiled with" the source (via
 * {@code GameData.exiledCards} by source ID), so the source can later
 * count and return those cards. The card that died is the stack entry's triggering card.
 */
public record ExileTriggeringCreatureAndTrackWithSourceEffect() implements CardEffect {
}
