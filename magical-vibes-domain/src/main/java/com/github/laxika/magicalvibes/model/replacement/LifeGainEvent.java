package com.github.laxika.magicalvibes.model.replacement;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntryType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Life a player is about to gain while replacement effects such as Heron of Hope or Boon
 * Reflection modify the amount. The gaining player orders the effects (CR 616.1).
 *
 * @param playerId        the player gaining life, who orders the effects
 * @param amount          the life to be gained as the event currently stands
 * @param source          the source description carried to the log, or {@code null}
 * @param sourceCard      the card that caused the gain, carried to the life-gain triggers, or {@code null}
 * @param sourceEntryType the kind of stack entry that caused the gain, or {@code null}
 * @param loseInstead     {@code true} once a replacement (Plague Drone, Tainted Remedy) has turned the gain into
 *                        losing {@code amount} life, so no life is gained and no other life-gain replacement applies
 * @param drawInstead     {@code true} once a replacement (Nefarious Lich) has turned the gain into drawing
 *                        {@code amount} cards, so no life is gained and no other life-gain replacement applies
 */
public record LifeGainEvent(UUID playerId, int amount, Set<String> appliedKeys, String source,
                            Card sourceCard, StackEntryType sourceEntryType,
                            boolean drawInstead, boolean loseInstead) implements ReplacementEvent {

    public LifeGainEvent {
        appliedKeys = Set.copyOf(appliedKeys);
    }

    @Override
    public UUID chooserId() {
        return playerId;
    }

    public LifeGainEvent withAmount(int newAmount) {
        return new LifeGainEvent(playerId, newAmount, appliedKeys, source, sourceCard, sourceEntryType, drawInstead, loseInstead);
    }

    public LifeGainEvent withDrawInstead() {
        return new LifeGainEvent(playerId, amount, appliedKeys, source, sourceCard, sourceEntryType, true, loseInstead);
    }

    public LifeGainEvent withLoseInstead() {
        return new LifeGainEvent(playerId, amount, appliedKeys, source, sourceCard, sourceEntryType, drawInstead, true);
    }

    @Override
    public LifeGainEvent withApplied(String key) {
        Set<String> keys = new HashSet<>(appliedKeys);
        keys.add(key);
        return new LifeGainEvent(playerId, amount, keys, source, sourceCard, sourceEntryType, drawInstead, loseInstead);
    }

    @Override
    public LifeGainEvent withoutAppliedKeys() {
        return new LifeGainEvent(playerId, amount, Set.of(), source, sourceCard, sourceEntryType, drawInstead, loseInstead);
    }
}
