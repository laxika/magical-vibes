package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import java.util.List;
import java.util.UUID;

/**
 * Deals damage to one or more players (never creatures). The {@link DamageRecipient} selects
 * which player(s) receive the damage; the {@link DynamicAmount} is evaluated at resolution
 * (fixed number, cards in a hand, +1/+1 counter count, …). For {@code EACH_PLAYER}, the amount
 * is evaluated once per player so player-relative scopes such as {@code CONTROLLER} refer to the
 * player receiving the damage.
 *
 * <p>{@code attachedCountFilter} is non-null only for the {@code ENCHANTED_PLAYER} curse case
 * (Curse of Thirst): the amount dealt is the number of permanents attached to the enchanted
 * player that match the predicate, and {@code amount} is ignored.
 *
 * @param unpreventable whether the damage can't be prevented
 */
public record DealDamageToPlayersEffect(DynamicAmount amount, DamageRecipient recipient,
                                        PermanentPredicate attachedCountFilter, boolean unpreventable,
                                        boolean recordDamageDealt, List<UUID> selectedPlayerIds)
        implements DamageDealingEffect, AcceptedPlayersAwareEffect,
        CombatDamageTriggerContextEffect, TriggeringSpellManaValueEffect {

    public DealDamageToPlayersEffect(DynamicAmount amount, DamageRecipient recipient,
                                     PermanentPredicate attachedCountFilter, boolean unpreventable) {
        this(amount, recipient, attachedCountFilter, unpreventable, false, List.of());
    }

    /** Records actual damage in the entry's event value for a subsequent effect. */
    public DealDamageToPlayersEffect recordingDamageDealt() {
        return new DealDamageToPlayersEffect(amount, recipient, attachedCountFilter, unpreventable, true,
                selectedPlayerIds);
    }

    public DealDamageToPlayersEffect(int damage, DamageRecipient recipient) {
        this(new Fixed(damage), recipient, null, false);
    }

    public DealDamageToPlayersEffect(DynamicAmount amount, DamageRecipient recipient) {
        this(amount, recipient, null, false);
    }

    public DealDamageToPlayersEffect(DynamicAmount amount, DamageRecipient recipient,
                                     PermanentPredicate attachedCountFilter) {
        this(amount, recipient, attachedCountFilter, false);
    }

    public DealDamageToPlayersEffect(DynamicAmount amount, DamageRecipient recipient,
                                     boolean unpreventable) {
        this(amount, recipient, null, unpreventable);
    }

    public DealDamageToPlayersEffect {
        selectedPlayerIds = List.copyOf(selectedPlayerIds);
    }

    public static DealDamageToPlayersEffect selectedOpponents(DynamicAmount amount) {
        return new DealDamageToPlayersEffect(amount, DamageRecipient.SELECTED_OPPONENTS,
                null, false, false, List.of());
    }

    public static DealDamageToPlayersEffect selectedOpponents(int damage) {
        return selectedOpponents(new Fixed(damage));
    }

    public DealDamageToPlayersEffect withSelectedPlayerIds(List<UUID> playerIds) {
        return new DealDamageToPlayersEffect(amount, recipient, attachedCountFilter, unpreventable,
                recordDamageDealt, playerIds);
    }

    @Override
    public DealDamageToPlayersEffect withAcceptedPlayerIds(List<UUID> playerIds) {
        if (recipient != DamageRecipient.SELECTED_OPPONENTS) {
            throw new IllegalStateException("Accepted player ids require SELECTED_OPPONENTS");
        }
        return withSelectedPlayerIds(playerIds);
    }

    /**
     * Deals damage to the enchanted player equal to the number of permanents attached to that
     * player matching the predicate (e.g. {@code PermanentHasSubtypePredicate(CardSubtype.CURSE)}
     * for Curse of Thirst).
     */
    public static DealDamageToPlayersEffect enchantedAttachedCount(PermanentPredicate predicate) {
        return new DealDamageToPlayersEffect(new Fixed(0), DamageRecipient.ENCHANTED_PLAYER, predicate);
    }

    // Per-recipient targeting: only TARGET_PLAYER chooses a player; TARGET_PERMANENT_CONTROLLER
    // rides the shared permanent target of a companion effect (e.g. Chandra's Outrage), so it takes
    // no independent target. TARGET_SPELL_CONTROLLER owns a spell-on-stack target (Refuse). The kept
    // @ValidatesTarget validator (DamageTargetValidators) enforces "must be a player" for
    // TARGET_PLAYER — a check the no-op PLAYER spec cannot express. Benign: the validator performs
    // no protection check (the damage lands on a player, not the permanent/spell).
    @Override
    public TargetSpec targetSpec() {
        return switch (recipient) {
            case TARGET_PLAYER -> TargetSpec.benign(TargetPredicates.player());
            case TARGET_PERMANENT_CONTROLLER -> TargetSpec.benign(TargetPredicates.permanent());
            case TARGET_SPELL_CONTROLLER -> TargetSpec.benign(TargetPredicates.spellOnStack());
            default -> TargetSpec.NONE;
        };
    }

    @Override
    public DynamicAmount damageAmount() {
        return amount;
    }

    @Override
    public boolean canDamageCreatures() {
        return false;
    }

    @Override
    public boolean canDamagePlayers() {
        return true;
    }

    @Override
    public boolean damagesController() {
        return recipient == DamageRecipient.CONTROLLER;
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return switch (recipient) {
            case TARGET_PLAYER, EACH_OTHER_OPPONENT -> TriggerContext.DAMAGED_PLAYER;
            case CHOSEN_PLAYER -> TriggerContext.CHOSEN_PLAYER;
            default -> null;
        };
    }
}
