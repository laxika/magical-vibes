package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Discards the identified card if it is still in the resolving controller's hand. */
public record DiscardSpecificCardEffect(UUID cardId) implements CardEffect {
}
