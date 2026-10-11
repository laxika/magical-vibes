package com.github.laxika.magicalvibes.model.replacement;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Damage about to be dealt while replacement and prevention effects modify it (CR 120.4b). The
 * damaged player, or the damaged permanent's controller, orders the effects (CR 616.1); a
 * redirection changes the recipient and with it the chooser (CR 616.2).
 *
 * <p>Combat damage is one event per recipient carrying a {@link DamagePortion} per source, since
 * all combat damage of a step is dealt simultaneously (CR 510.2); {@link #amount()} is then the
 * portions' sum. Other damage has a single source, the stack entry, and no portions.
 *
 * @param entry                   the stack entry dealing the damage; for combat damage, the first
 *                                portion's source as a combat-damage entry
 * @param recipientPlayerId       the player the damage would be dealt to, or for a permanent its
 *                                controller when the event began
 * @param recipientPermanentId    the permanent the damage would be dealt to; {@code null} for a player
 * @param explicitSource          the permanent dealing the damage when it isn't the entry's source
 *                                (a fight or a bite), otherwise {@code null}
 * @param amount                  the damage still to be dealt
 * @param unpreventable           the damage can't be prevented, whatever the board says later
 * @param cantBeRedirected        the damage can't be redirected
 * @param journalSlot             the slot of the resolving effect's {@code DamageJournal} this
 *                                event fills, or -1
 * @param combat                  the event is combat damage
 * @param portions                the per-source parts of combat damage; empty otherwise
 * @param redirected              combat damage a redirection effect sent here from another
 *                                recipient (CR 614.9); still combat damage of the same step
 */
public record DamageEvent(StackEntry entry, UUID recipientPlayerId, UUID recipientPermanentId,
                          Permanent explicitSource, int amount, boolean unpreventable, boolean cantBeRedirected,
                          int journalSlot, Set<String> appliedKeys,
                          boolean combat, List<DamagePortion> portions, boolean redirected)
        implements ReplacementEvent {

    public DamageEvent {
        appliedKeys = Set.copyOf(appliedKeys);
        portions = List.copyOf(portions);
        if (!portions.isEmpty()) {
            amount = portions.stream().mapToInt(DamagePortion::amount).sum();
        }
    }

    /** Damage to a player. */
    public static DamageEvent toPlayer(StackEntry entry, UUID playerId, int amount, boolean unpreventable) {
        return new DamageEvent(entry, playerId, null, null, amount, unpreventable, false,
                -1, Set.of(), false, List.of(), false);
    }

    /** Damage to a permanent controlled by {@code controllerId}. */
    public static DamageEvent toPermanent(StackEntry entry, UUID controllerId, UUID permanentId,
                                          Permanent explicitSource, int amount, boolean unpreventable,
                                          boolean cantBeRedirected) {
        return new DamageEvent(entry, controllerId, permanentId, explicitSource, amount, unpreventable,
                cantBeRedirected, -1, Set.of(), false, List.of(), false);
    }

    /** The combat damage one recipient is dealt in a damage step. */
    public static DamageEvent combat(StackEntry entry, UUID chooserId, UUID recipientPermanentId,
                                     boolean unpreventable, boolean cantBeRedirected, List<DamagePortion> portions) {
        return new DamageEvent(entry, chooserId, recipientPermanentId, null, 0, unpreventable, cantBeRedirected,
                -1, Set.of(), true, portions, false);
    }

    public boolean toPermanent() {
        return recipientPermanentId != null;
    }

    /** Combat damage from more than one source, so effects act per source or on the whole. */
    public boolean hasPortions() {
        return !portions.isEmpty();
    }

    @Override
    public UUID chooserId() {
        return recipientPlayerId;
    }

    /**
     * This event with {@code newAmount} damage left. Combat damage spreads the change over its
     * portions: a multiple scales every portion, anything less is taken from the portions in order.
     */
    public DamageEvent withAmount(int newAmount) {
        int target = Math.max(0, newAmount);
        if (!hasPortions()) {
            return with(target, portions, recipientPlayerId, recipientPermanentId, appliedKeys);
        }
        return with(target, spread(portions, target, false), recipientPlayerId, recipientPermanentId, appliedKeys);
    }

    /** The damage that can still be prevented: everything unless a portion's source can't be. */
    public int preventableAmount() {
        if (!hasPortions()) {
            return unpreventable ? 0 : amount;
        }
        return portions.stream().filter(portion -> !portion.unpreventable()).mapToInt(DamagePortion::amount).sum();
    }

    /** This event after a prevention left {@code remaining} of its preventable damage. */
    public DamageEvent withPreventableAmount(int remaining) {
        if (!hasPortions()) {
            return withAmount(remaining);
        }
        return with(0, spread(portions, Math.max(0, remaining), true), recipientPlayerId, recipientPermanentId,
                appliedKeys);
    }

    /** This event with portion {@code index} at {@code newAmount}. */
    public DamageEvent withPortionAmount(int index, int newAmount) {
        List<DamagePortion> updated = new ArrayList<>(portions);
        updated.set(index, updated.get(index).withAmount(newAmount));
        return with(0, updated, recipientPlayerId, recipientPermanentId, appliedKeys);
    }

    /** The same damage dealt to another player instead (CR 614.9). */
    public DamageEvent withRecipient(UUID newRecipientPlayerId) {
        return with(amount, portions, newRecipientPlayerId, null, appliedKeys);
    }

    /**
     * {@code redirectedAmount} of this noncombat damage dealt to another player or permanent instead
     * (CR 614.9), as an event of its own for the new recipient's effects. It keeps the source and
     * whether the damage can be prevented or redirected; an effect that already applied stays spent
     * (CR 614.5) when {@code stillSpent} accepts its key.
     *
     * @param controllerId the new recipient player, or the new recipient permanent's controller
     * @param permanentId  the new recipient permanent, or {@code null} for a player
     */
    public DamageEvent redirectedTo(UUID controllerId, UUID permanentId, int redirectedAmount,
                                    Predicate<String> stillSpent) {
        Set<String> keys = new HashSet<>();
        for (String key : appliedKeys) {
            if (stillSpent.test(key)) {
                keys.add(key);
            }
        }
        return new DamageEvent(entry, controllerId, permanentId, explicitSource, Math.max(0, redirectedAmount),
                unpreventable, cantBeRedirected, -1, keys, false, List.of(), false);
    }

    /**
     * This combat damage as damage a redirection effect sent to its recipient from another one
     * (CR 614.9), with the effects {@code spentKeys} names already applied to it (CR 614.5).
     */
    public DamageEvent asRedirectedCombat(Set<String> spentKeys) {
        return new DamageEvent(entry, recipientPlayerId, recipientPermanentId, explicitSource, amount, unpreventable,
                cantBeRedirected, journalSlot, spentKeys, combat, portions, true);
    }

    public DamageEvent withJournalSlot(int slot) {
        return new DamageEvent(entry, recipientPlayerId, recipientPermanentId, explicitSource, amount, unpreventable,
                cantBeRedirected, slot, appliedKeys, combat, portions, redirected);
    }

    @Override
    public DamageEvent withApplied(String key) {
        Set<String> keys = new HashSet<>(appliedKeys);
        keys.add(key);
        return with(amount, portions, recipientPlayerId, recipientPermanentId, keys);
    }

    @Override
    public DamageEvent withoutAppliedKeys() {
        return with(amount, portions, recipientPlayerId, recipientPermanentId, Set.of());
    }

    @Override
    public DamageEvent copyForSimulation() {
        return new DamageEvent(new StackEntry(entry), recipientPlayerId, recipientPermanentId,
                explicitSource == null ? null : new Permanent(explicitSource), amount, unpreventable,
                cantBeRedirected, journalSlot, appliedKeys, combat,
                portions.stream().map(DamagePortion::copyForSimulation).toList(), redirected);
    }

    private DamageEvent with(int newAmount, List<DamagePortion> newPortions, UUID playerId, UUID permanentId,
                             Set<String> keys) {
        return new DamageEvent(entry, playerId, permanentId, explicitSource, newAmount, unpreventable,
                cantBeRedirected, journalSlot, keys, combat, newPortions, redirected);
    }

    /**
     * Spreads a new total over {@code portions}. Raising it by a whole multiple scales each portion;
     * any other increase goes to the first portion. Lowering it takes damage away from the portions
     * in order, skipping portions that can't be prevented when {@code preventableOnly}.
     */
    private static List<DamagePortion> spread(List<DamagePortion> portions, int target, boolean preventableOnly) {
        List<DamagePortion> updated = new ArrayList<>(portions);
        int total = portions.stream()
                .filter(portion -> !preventableOnly || !portion.unpreventable())
                .mapToInt(DamagePortion::amount).sum();
        if (target >= total) {
            if (total > 0 && target % total == 0) {
                int factor = target / total;
                for (int i = 0; i < updated.size(); i++) {
                    DamagePortion portion = updated.get(i);
                    if (!preventableOnly || !portion.unpreventable()) {
                        updated.set(i, portion.withAmount(portion.amount() * factor));
                    }
                }
            } else if (target > total) {
                for (int i = 0; i < updated.size(); i++) {
                    DamagePortion portion = updated.get(i);
                    if (!preventableOnly || !portion.unpreventable()) {
                        updated.set(i, portion.withAmount(portion.amount() + target - total));
                        break;
                    }
                }
            }
            return updated;
        }
        int toRemove = total - target;
        for (int i = 0; i < updated.size() && toRemove > 0; i++) {
            DamagePortion portion = updated.get(i);
            if (preventableOnly && portion.unpreventable()) {
                continue;
            }
            int removed = Math.min(portion.amount(), toRemove);
            updated.set(i, portion.withAmount(portion.amount() - removed));
            toRemove -= removed;
        }
        return updated;
    }
}
