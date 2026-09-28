package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.UUID;

/** A finite player-scoped trigger created by a digital sacrifice boon. */
public record SacrificeBoonWatcher(UUID controllerId, Card sourceCard, CardEffect effect,
                                   int remainingUses) {
}
