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
 */
public record LifeGainEvent(UUID playerId, int amount, Set<String> appliedKeys, String source,
                            Card sourceCard, StackEntryType sourceEntryType) implements ReplacementEvent {

    public LifeGainEvent {
        appliedKeys = Set.copyOf(appliedKeys);
    }

    @Override
    public UUID chooserId() {
        return playerId;
    }

    public LifeGainEvent withAmount(int newAmount) {
        return new LifeGainEvent(playerId, newAmount, appliedKeys, source, sourceCard, sourceEntryType);
    }

    @Override
    public LifeGainEvent withApplied(String key) {
        Set<String> keys = new HashSet<>(appliedKeys);
        keys.add(key);
        return new LifeGainEvent(playerId, amount, keys, source, sourceCard, sourceEntryType);
    }

    @Override
    public LifeGainEvent withoutAppliedKeys() {
        return new LifeGainEvent(playerId, amount, Set.of(), source, sourceCard, sourceEntryType);
    }
}
