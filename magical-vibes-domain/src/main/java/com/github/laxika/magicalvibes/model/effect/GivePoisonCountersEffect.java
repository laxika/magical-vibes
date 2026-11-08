package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;


/**
 * Gives poison counters to one or more players. A single record covers the whole give-poison
 * family: the {@link PoisonRecipient} routes who gets poisoned (controller / target player /
 * each player / each opponent / the targeted permanent's controller / the enchanted permanent's
 * controller).
 *
 * @param amount           number of poison counters to give
 * @param recipient        who gets the poison counters
 */
public record GivePoisonCountersEffect(DynamicAmount amount, PoisonRecipient recipient)
        implements CardEffect, CombatDamageTriggerContextEffect {

    public GivePoisonCountersEffect(int amount, PoisonRecipient recipient) {
        this(new Fixed(amount), recipient);
    }

    @Override
    public TargetSpec targetSpec() {
        return recipient == PoisonRecipient.TARGET_PLAYER
                ? TargetSpec.benign(TargetPredicates.player())
                : TargetSpec.NONE;
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        // On an Triggers.dealsCombatDamageToPlayer() trigger (Pit Scorpion) the TARGET_PLAYER recipient must
        // bind to the damaged player so the poison counter lands on them.
        return recipient == PoisonRecipient.TARGET_PLAYER ? TriggerContext.DAMAGED_PLAYER : null;
    }
}
