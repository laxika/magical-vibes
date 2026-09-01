package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Creates token copies of the permanent attached to an Aura. */
public record CreateTokenCopyOfEnchantedPermanentEffect(int amount, UUID auraPermanentId) implements CardEffect {

    public CreateTokenCopyOfEnchantedPermanentEffect() {
        this(1, null);
    }

    public CreateTokenCopyOfEnchantedPermanentEffect(UUID auraPermanentId) {
        this(1, auraPermanentId);
    }

    public CreateTokenCopyOfEnchantedPermanentEffect(int amount) {
        this(amount, null);
    }
}
