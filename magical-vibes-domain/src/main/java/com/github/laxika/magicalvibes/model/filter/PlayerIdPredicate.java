package com.github.laxika.magicalvibes.model.filter;

import java.util.UUID;

/** Restricts a player target to one exact player. */
public record PlayerIdPredicate(UUID playerId) implements PlayerPredicate {
}
