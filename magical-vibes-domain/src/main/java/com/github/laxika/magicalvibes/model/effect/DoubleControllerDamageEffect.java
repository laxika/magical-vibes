package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;

/**
 * Static effect that doubles damage dealt by sources controlled by this permanent's controller.
 * Unlike {@link DoubleDamageEffect} which doubles all damage globally, this only affects
 * sources controlled by the same player who controls this permanent.
 *
 * <p>The {@code stackFilter} predicate restricts which stack-based damage sources (spells/abilities)
 * are doubled. A {@code null} filter matches all stack entries.
 *
 * <p>The {@code appliesToCombatDamage} flag controls whether combat damage is also doubled; {@code multiplier} is 2
 * for "double" and 3 for "triple" (Fiery Emancipation, City on Fire). Floated on a player by
 * {@link GrantStaticEffectToPlayerEffect} it is "if a source you control would deal damage this turn" (Insult, Quest for
 * Pure Flame): a rules change, so it applies to sources that weren't there when it began (CR 611.2c).
 *
 * <p>Examples:
 * <ul>
 *   <li>Angrath's Marauders: {@code new DoubleControllerDamageEffect(null, true)} — doubles all damage</li>
 *   <li>Fire Servant: {@code new DoubleControllerDamageEffect(redInstantOrSorceryFilter, false)} — doubles only red instant/sorcery damage</li>
 * </ul>
 */
public record DoubleControllerDamageEffect(int multiplier, StackEntryPredicate stackFilter, boolean appliesToCombatDamage)
        implements ControllerDamageMultiplyingEffect {

    public DoubleControllerDamageEffect {
        if (multiplier < 2) {
            throw new IllegalArgumentException("Damage multiplier must be at least 2");
        }
    }

    public DoubleControllerDamageEffect(StackEntryPredicate stackFilter, boolean appliesToCombatDamage) {
        this(2, stackFilter, appliesToCombatDamage);
    }

    @Override
    public int damageMultiplier() {
        return multiplier;
    }

    @Override
    public boolean isDoublingEffect() {
        return multiplier == 2;
    }
}
