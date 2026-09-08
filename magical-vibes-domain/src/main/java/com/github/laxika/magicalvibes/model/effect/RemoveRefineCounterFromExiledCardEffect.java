package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Removes one refine counter from a card in exile and offers its last-counter cast. */
public record RemoveRefineCounterFromExiledCardEffect(UUID cardId) implements CardEffect {
}
