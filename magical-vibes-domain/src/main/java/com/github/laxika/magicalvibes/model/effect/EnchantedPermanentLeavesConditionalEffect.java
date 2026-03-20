package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;

/**
 * Trigger descriptor for "when enchanted permanent leaves the battlefield, if [condition], [effects]"
 * abilities. Used on the {@code ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD} slot.
 *
 * <p>When the enchanted permanent leaves the battlefield (graveyard, exile, hand, or library),
 * the {@code permanentFilter} is checked against the leaving permanent's card. If it matches
 * (or is null for unconditional triggers), the {@code resolvedEffects} are put on the stack.
 *
 * @param permanentFilter predicate to check against the leaving permanent's card (null = always fires)
 * @param resolvedEffects effects to put on the stack when this triggers
 */
public record EnchantedPermanentLeavesConditionalEffect(
        CardPredicate permanentFilter,
        List<CardEffect> resolvedEffects
) implements CardEffect {
}
