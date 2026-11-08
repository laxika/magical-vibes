package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Trigger-condition record for the {@code Triggers.creatureYouControlDealsDamageToCreature()} ability (Greatbow Doyen).
 *
 * <p>The normal form means "Whenever a [creature matching {@code sourceFilter}] you control deals
 * damage to a creature, that creature deals that much damage to that creature's controller." The
 * trigger collector judges the filters and puts {@link #triggeredEffect()} on the stack with the
 * damage-source creature as the entry's source, the damaged creature as its triggering permanent and
 * the damage dealt as its event value; the record itself is never resolved.
 *
 * <p>The self-scoped combat-life-loss form is used by Flayed Nim. It keeps the same trigger
 * collector path while changing the result to life loss and restricting it to combat damage.
 */
public record ReflectAllyDamageToDamagedCreatureControllerEffect(
        PermanentPredicate sourceFilter,
        boolean combatOnly,
        boolean sourceMustBeWatcher,
        boolean lifeLoss) implements CardEffect {

    public ReflectAllyDamageToDamagedCreatureControllerEffect(PermanentPredicate sourceFilter) {
        this(sourceFilter, false, false, false);
    }

    public static ReflectAllyDamageToDamagedCreatureControllerEffect selfCombatDamageCausesLifeLoss() {
        return new ReflectAllyDamageToDamagedCreatureControllerEffect(null, true, true, true);
    }

    /**
     * What the ability does, read from its stack entry as it resolves: "that much" is the entry's event value and
     * "that creature's controller" the controller of its triggering permanent (the damaged creature) then, or as it
     * last existed.
     */
    public CardEffect triggeredEffect() {
        return lifeLoss
                ? new LoseLifeEffect(new EventValue(), LoseLifeRecipient.TRIGGERING_PERMANENT_CONTROLLER)
                : new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER);
    }
}
