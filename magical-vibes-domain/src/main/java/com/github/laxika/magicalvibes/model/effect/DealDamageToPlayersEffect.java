package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Deals damage to one or more players (never creatures). The {@link DamageRecipient} selects
 * which player(s) receive the damage; the {@link DynamicAmount} is evaluated at resolution
 * (fixed number, cards in a hand, +1/+1 counter count, …) — the same value is dealt to every
 * player in scope.
 *
 * <p>{@code attachedCountFilter} is non-null only for the {@code ENCHANTED_PLAYER} curse case
 * (Curse of Thirst): the amount dealt is the number of permanents attached to the enchanted
 * player that match the predicate, and {@code amount} is ignored.
 */
public record DealDamageToPlayersEffect(DynamicAmount amount, DamageRecipient recipient,
                                        PermanentPredicate attachedCountFilter)
        implements DamageDealingEffect, CombatDamageTriggerContextEffect {

    public DealDamageToPlayersEffect(int damage, DamageRecipient recipient) {
        this(new Fixed(damage), recipient, null);
    }

    public DealDamageToPlayersEffect(DynamicAmount amount, DamageRecipient recipient) {
        this(amount, recipient, null);
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
    // no independent target. The kept @ValidatesTarget validator (DamageTargetValidators) enforces
    // "must be a player" for TARGET_PLAYER — a check the no-op PLAYER spec cannot express. Benign:
    // the validator performs no protection check (the damage lands on a player, not the permanent).
    @Override
    public TargetSpec targetSpec() {
        return switch (recipient) {
            case TARGET_PLAYER -> TargetSpec.benign(TargetCategory.PLAYER);
            case TARGET_PERMANENT_CONTROLLER -> TargetSpec.benign(TargetCategory.PERMANENT);
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
    public TriggerContext combatDamageTriggerContext() {
        return recipient == DamageRecipient.TARGET_PLAYER ? TriggerContext.DAMAGED_PLAYER : null;
    }
}
