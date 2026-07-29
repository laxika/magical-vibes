package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * {@code ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD}: the dying enchanted creature's controller
 * loses life. On the card, {@code amount} of 0 means "equal to the dying creature's toughness"
 * (Banewasp Affliction); a positive {@code amount} is a fixed loss (Decomposition — 2 life).
 * {@code DeathTriggerCollectorService} bakes the resolved amount and controller into the stack copy.
 */
public record EnchantedCreatureControllerLosesLifeEffect(int amount, UUID affectedPlayerId) implements CardEffect {

    public EnchantedCreatureControllerLosesLifeEffect(int amount) {
        this(amount, null);
    }
}
