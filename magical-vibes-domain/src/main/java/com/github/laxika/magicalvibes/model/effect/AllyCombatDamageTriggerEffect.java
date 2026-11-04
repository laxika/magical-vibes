package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * An event binding on a "whenever a creature you control deals combat damage to a player [or battle]" ability
 * (effect-slot-refactor.md step 15 replaces it with an effect that reads the event). Don't author the trigger-level
 * fields: the creature's filter is the trigger's ({@code Triggers.creatureYouControlDealsCombatDamageToPlayer()
 * .withFilter(…)}) and "one or more creatures" is {@code TriggerOptions.ONE_OR_MORE}; the collectors read both from
 * the ability ({@code CombatAbilityReader.allyCombatDamage}), so a plain effect is the usual form.
 * <p>
 * What a card still authors is how the effect reads the event: {@link #boundToDealer} for "put a +1/+1 counter on it
 * / that creature" (the creature that dealt the damage is the effect's source) and {@link #opponentsDealtDamage} for
 * "whenever your opponents are dealt combat damage" (any player's creatures, but the damaged player is an opponent of
 * the ability's controller).
 *
 * @param dealerPredicate      what the creature that dealt the damage must be, or {@code null} for any
 * @param bindSourceToDealer   the creature that dealt the damage is the effect's source
 * @param oncePerDamageStep    once for each combat damage step however many creatures deal damage
 * @param opponentDealtDamage  "whenever your opponents are dealt combat damage"
 */
public record AllyCombatDamageTriggerEffect(PermanentPredicate dealerPredicate, CardEffect effect,
                                            boolean bindSourceToDealer,
                                            boolean oncePerDamageStep,
                                            boolean opponentDealtDamage) implements CardEffect {

    public AllyCombatDamageTriggerEffect(PermanentPredicate dealerPredicate, CardEffect effect,
                                         boolean bindSourceToDealer, boolean oncePerDamageStep) {
        this(dealerPredicate, effect, bindSourceToDealer, oncePerDamageStep, false);
    }

    public AllyCombatDamageTriggerEffect(PermanentPredicate dealerPredicate, CardEffect effect) {
        this(dealerPredicate, effect, false, false);
    }

    public AllyCombatDamageTriggerEffect(PermanentPredicate dealerPredicate, CardEffect effect,
                                         boolean bindSourceToDealer) {
        this(dealerPredicate, effect, bindSourceToDealer, false);
    }

    /** {@code effect} acts on the creature that dealt the damage: "put a +1/+1 counter on that creature". */
    public static AllyCombatDamageTriggerEffect boundToDealer(CardEffect effect) {
        return new AllyCombatDamageTriggerEffect(null, effect, true);
    }

    /**
     * "Whenever your opponents are dealt combat damage": a creature of any player deals the damage, to an opponent of
     * the ability's controller.
     */
    public static AllyCombatDamageTriggerEffect opponentsDealtDamage(CardEffect effect) {
        return new AllyCombatDamageTriggerEffect(null, effect, false, false, true);
    }

    /** This record around {@code effect} in place of its effect. */
    public AllyCombatDamageTriggerEffect withEffect(CardEffect effect) {
        return new AllyCombatDamageTriggerEffect(dealerPredicate, effect, bindSourceToDealer, oncePerDamageStep,
                opponentDealtDamage);
    }
}
