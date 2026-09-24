package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Makes the controller discard a specific card if it is still in their hand. */
public record DiscardSpecificCardEffect(UUID cardId) implements CardEffect {
}
