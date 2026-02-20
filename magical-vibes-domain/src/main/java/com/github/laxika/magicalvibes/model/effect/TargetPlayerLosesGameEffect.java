package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

public record TargetPlayerLosesGameEffect(UUID playerId) implements CardEffect {
}
