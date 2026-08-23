package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Removes one time counter from a suspended card and offers its owner a cast without paying its mana cost. */
public record RemoveTimeCounterFromExiledCardEffect(UUID cardId) implements CardEffect {
}
