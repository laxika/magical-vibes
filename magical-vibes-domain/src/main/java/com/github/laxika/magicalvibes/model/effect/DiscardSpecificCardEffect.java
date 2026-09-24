package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Discards the identified card from the resolving controller's hand, if it is still there. */
public record DiscardSpecificCardEffect(UUID cardId) implements CardEffect {
}
