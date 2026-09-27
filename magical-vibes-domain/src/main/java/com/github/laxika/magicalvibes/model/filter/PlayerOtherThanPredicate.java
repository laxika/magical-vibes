package com.github.laxika.magicalvibes.model.filter;

import java.util.UUID;

/** Restricts a player target to players other than the supplied player. */
public record PlayerOtherThanPredicate(UUID excludedPlayerId) implements PlayerPredicate {
}
