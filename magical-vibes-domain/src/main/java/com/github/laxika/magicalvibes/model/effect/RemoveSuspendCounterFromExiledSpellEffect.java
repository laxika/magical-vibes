package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Removes a suspend counter from an exiled spell during its owner's upkeep. */
public record RemoveSuspendCounterFromExiledSpellEffect(UUID cardId) implements CardEffect {
}
