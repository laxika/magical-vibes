package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Describes a combat-damage trigger whose dynamic amount can read the damage event.
 *
 * <p>The combat trigger collector snapshots the damage dealt onto the stack entry when the
 * effect's amount references {@code EventValue}. This also covers triggers supplied by an Aura,
 * whose effect is collected through the attached-permanent path.
 */
public interface CombatDamageAmountAwareEffect extends CardEffect {

    DynamicAmount combatDamageAmount();

    /**
     * Materializes any combat-damage-dependent restriction that must be fixed when the trigger is
     * created. Effects that only consume the event amount when they resolve keep their original
     * form.
     */
    default CardEffect snapshotCombatDamage(int damageDealt) {
        return this;
    }
}
