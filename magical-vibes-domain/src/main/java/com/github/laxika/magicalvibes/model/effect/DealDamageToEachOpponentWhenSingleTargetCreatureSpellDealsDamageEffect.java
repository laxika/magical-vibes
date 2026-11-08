package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.EventValue;

/**
 * Trigger-condition record for Imodane's "whenever an instant or sorcery spell you control that targets only a single
 * creature deals damage to that creature". The collector puts {@link #triggeredEffect()} on the stack with the damage
 * dealt to that creature as its event value.
 */
public record DealDamageToEachOpponentWhenSingleTargetCreatureSpellDealsDamageEffect()
        implements CardEffect {

    /** "That much damage to each opponent", read from the stack entry. */
    public CardEffect triggeredEffect() {
        return new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.EACH_OPPONENT);
    }
}
