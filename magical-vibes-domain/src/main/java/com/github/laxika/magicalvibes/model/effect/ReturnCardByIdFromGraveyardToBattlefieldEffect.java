package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Returns a specific card from a graveyard to the battlefield without targeting it. */
public record ReturnCardByIdFromGraveyardToBattlefieldEffect(UUID cardId) implements CardEffect {
}
