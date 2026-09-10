package com.github.laxika.magicalvibes.model.filter;

import java.util.UUID;

/** Restricts a permanent to the player fixed when the effect was created. */
public record PermanentControlledByPlayerPredicate(UUID playerId) implements PermanentPredicate {
}
