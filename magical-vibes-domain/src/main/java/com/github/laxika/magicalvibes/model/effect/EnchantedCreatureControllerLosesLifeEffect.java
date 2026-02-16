package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

public record EnchantedCreatureControllerLosesLifeEffect(int amount, UUID affectedPlayerId) implements CardEffect {

    public EnchantedCreatureControllerLosesLifeEffect(int amount) {
        this(amount, null);
    }
}
