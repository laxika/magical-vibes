package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Internal follow-up that makes a specific player draw one card. */
public record DrawCardForPlayerEffect(UUID playerId) implements CardEffect {
}
