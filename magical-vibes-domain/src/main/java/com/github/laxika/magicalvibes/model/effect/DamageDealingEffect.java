package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Capability interface for effects that deal a single evaluated amount of damage to one
 * target category (creatures and/or players). Lets consumers — chiefly the AI evaluators —
 * ask "how much damage, and to what" without knowing the concrete effect type, mirroring how
 * {@link ManaProducingEffect} abstracts mana production.
 *
 * <p>Descriptive only: it states facts drawn from the record's existing components, never a
 * score. All methods must be answerable from those components.
 *
 * <p>Scope note: this covers the "deal a {@link DynamicAmount} to a single target category"
 * shape. Damage effects that split a total among many targets ({@code DealDividedDamageEffect})
 * or hit every creature/player at once ({@code MassDamageEffect}) do not fit that shape and
 * deliberately do not implement this interface.
 */
public interface DamageDealingEffect extends CardEffect {

    /**
     * The amount of damage dealt to each affected target, as a {@link DynamicAmount} evaluated
     * at resolution (fixed number, X paid, source power/toughness, counter counts, …).
     */
    DynamicAmount damageAmount();

    /** Whether this effect can deal its damage to creatures. */
    boolean canDamageCreatures();

    /** Whether this effect can deal its damage to players. */
    boolean canDamagePlayers();
}
