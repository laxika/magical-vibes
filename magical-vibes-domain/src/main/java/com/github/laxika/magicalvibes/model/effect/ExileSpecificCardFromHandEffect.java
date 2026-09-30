package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Exiles the identified card from the resolving controller's hand if it is still there. */
public record ExileSpecificCardFromHandEffect(UUID cardId) implements CardEffect {
}
