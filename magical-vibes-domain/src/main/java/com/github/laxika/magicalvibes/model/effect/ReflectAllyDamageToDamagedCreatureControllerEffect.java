package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.EventValue;

/**
 * Trigger-condition record for "Whenever an Archer you control deals damage to a creature, that Archer deals that much
 * damage to that creature's controller" (Greatbow Doyen), written on
 * {@code Triggers.creatureYouControlDealsDamageToCreature().withFilter(…)}.
 *
 * <p>The creature that dealt the damage deals the reflected damage (CR 608.2h), not the ability's source, so its
 * collector puts {@link #triggeredEffect()} on the stack with that creature as the entry's source, the damaged
 * creature as its triggering permanent and the damage dealt as its event value; the record itself is never resolved.
 */
public record ReflectAllyDamageToDamagedCreatureControllerEffect() implements CardEffect {

    /**
     * What the ability does, read from its stack entry as it resolves: "that much" is the entry's event value and
     * "that creature's controller" the controller of its triggering permanent (the damaged creature) then, or as it
     * last existed.
     */
    public CardEffect triggeredEffect() {
        return new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER);
    }
}
