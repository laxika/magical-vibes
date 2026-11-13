package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.condition.Condition;

/**
 * "You may exert this creature as it attacks": a static ability whose exertion is an optional cost to attack
 * (CR 701.43d, 508.1g), chosen as the creature is declared as an attacker. A "when you do" printed with it is the
 * linked triggered ability {@code Triggers.exertedAsItAttacks()} (CR 607.2h).
 *
 * @param condition what must be true for the creature to be exerted as it attacks ("if this creature hasn't been
 *                  exerted this turn", Combat Celebrant), judged for the attacking creature, or {@code null}
 */
public record ExertAsItAttacksEffect(Condition condition) implements CardEffect {

    /** "You may exert this creature as it attacks", always. */
    public ExertAsItAttacksEffect() {
        this(null);
    }
}
