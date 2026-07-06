package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.UUID;

/**
 * Gives poison counters to one or more players. A single record covers the whole give-poison
 * family: the {@link PoisonRecipient} routes who gets poisoned (controller / target player /
 * each player / the enchanted permanent's controller).
 *
 * <p>When {@code recipient == TARGET_PLAYER} and {@code spellFilter} is non-null, this doubles as a
 * trigger descriptor for {@code ON_CONTROLLER_CASTS_SPELL}: the trigger fires only when the
 * controller casts a spell matching the predicate. {@code SpellCastTriggerCollectorService}
 * resolves it into a stack entry with a resolution-only copy ({@code spellFilter == null}).
 *
 * <p>When {@code recipient == ENCHANTED_PERMANENT_CONTROLLER}, {@code affectedPlayerId} is
 * {@code null} in the card definition and gets baked in at trigger time by
 * {@code MiscTriggerCollectorService.handleEnchantedPermanentTapPoison} (Relic Putrescence).
 *
 * @param amount           number of poison counters to give
 * @param recipient        who gets the poison counters
 * @param spellFilter      trigger-descriptor filter (target-player spell-cast triggers only)
 * @param affectedPlayerId enchanted permanent's controller, baked in at trigger time
 */
public record GivePoisonCountersEffect(int amount, PoisonRecipient recipient,
        CardPredicate spellFilter, UUID affectedPlayerId) implements CardEffect {

    public GivePoisonCountersEffect(int amount, PoisonRecipient recipient) {
        this(amount, recipient, null, null);
    }

    /** Target-player trigger-descriptor constructor. */
    public GivePoisonCountersEffect(int amount, PoisonRecipient recipient, CardPredicate spellFilter) {
        this(amount, recipient, spellFilter, null);
    }

    @Override
    public boolean canTargetPlayer() {
        return recipient == PoisonRecipient.TARGET_PLAYER;
    }
}
