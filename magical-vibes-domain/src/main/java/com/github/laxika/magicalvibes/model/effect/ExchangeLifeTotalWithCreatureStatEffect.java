package com.github.laxika.magicalvibes.model.effect;

/**
 * Exchanges a player's life total with the source creature's power or toughness.
 * Per CR 701.10e: the player gets a new life total equal to the creature's stat,
 * and the creature's stat simultaneously becomes the player's former life total
 * (as a layer 7b setting effect).
 * Uses {@code sourcePermanentId} on the stack entry to identify the creature.
 *
 * @param stat      POWER (Evra) or TOUGHNESS (Tree of Redemption / Tree of Perdition)
 * @param recipient CONTROLLER (your life) or TARGET_PLAYER (target opponent's life)
 */
public record ExchangeLifeTotalWithCreatureStatEffect(Stat stat, Recipient recipient) implements CardEffect {

    public enum Stat {
        POWER, TOUGHNESS
    }

    public enum Recipient {
        CONTROLLER, TARGET_PLAYER
    }

    /** Controller exchanges their own life (Tree of Redemption, Evra). */
    public ExchangeLifeTotalWithCreatureStatEffect(Stat stat) {
        this(stat, Recipient.CONTROLLER);
    }

    @Override
    public TargetSpec targetSpec() {
        return recipient == Recipient.TARGET_PLAYER
                ? TargetSpec.benign(TargetCategory.PLAYER)
                : TargetSpec.NONE;
    }
}
