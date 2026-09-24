package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Discards a particular card from the resolving player's hand, if it is still there. */
public record DiscardSpecificCardEffect(UUID cardId) implements CardEffect {
}
