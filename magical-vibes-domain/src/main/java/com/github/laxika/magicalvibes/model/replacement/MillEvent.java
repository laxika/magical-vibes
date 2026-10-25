package com.github.laxika.magicalvibes.model.replacement;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Cards a player is about to mill while replacement effects such as The Water Crystal's "that many
 * cards plus four" and Bruvac the Grandiloquent's "twice that many" modify the number. The milling
 * player orders the effects (CR 616.1); once all of them have applied, the player mills
 * {@code amount} cards, or as many as their library holds (CR 701.17b).
 *
 * @param playerId    the player milling, who orders the effects
 * @param amount      the number of cards to mill as the event currently stands
 * @param journalSlot the slot of the resolving effect's {@code DamageJournal} this mill fills, or
 *                    {@code -1} when it is not journaled and is milled as soon as its order is chosen
 */
public record MillEvent(UUID playerId, int amount, Set<String> appliedKeys, int journalSlot)
        implements ReplacementEvent {

    public MillEvent {
        appliedKeys = Set.copyOf(appliedKeys);
    }

    @Override
    public UUID chooserId() {
        return playerId;
    }

    public MillEvent withAmount(int newAmount) {
        return new MillEvent(playerId, newAmount, appliedKeys, journalSlot);
    }

    public MillEvent withJournalSlot(int slot) {
        return new MillEvent(playerId, amount, appliedKeys, slot);
    }

    @Override
    public MillEvent withApplied(String key) {
        Set<String> keys = new HashSet<>(appliedKeys);
        keys.add(key);
        return new MillEvent(playerId, amount, keys, journalSlot);
    }

    @Override
    public MillEvent withoutAppliedKeys() {
        return new MillEvent(playerId, amount, Set.of(), journalSlot);
    }
}
