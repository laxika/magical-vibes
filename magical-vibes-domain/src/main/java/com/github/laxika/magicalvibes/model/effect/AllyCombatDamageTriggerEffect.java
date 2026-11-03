package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The record the "whenever a creature you control deals combat damage to a player [or battle]" collectors read
 * (effect-slot-refactor.md steps 14 and 15). Don't author the trigger-level fields: the creature's filter is the
 * trigger's ({@code Triggers.creatureYouControlDealsCombatDamageToPlayer().withFilter(…)}) and "one or more creatures"
 * is {@code TriggerOptions.ONE_OR_MORE}; the legacy view ({@code TriggeredAbility.legacyEffects()}) wraps every effect
 * of such an ability in this record with them, so a plain effect is the usual form.
 * <p>
 * What a card still authors is how the effect reads the event, until step 15 makes it an event reference:
 * {@link #boundToDealer} for "put a +1/+1 counter on it / that creature" (the creature that dealt the damage is the
 * effect's source) and {@link #opponentsDealtDamage} for "whenever your opponents are dealt combat damage" (any
 * player's creatures, but the damaged player is an opponent of the ability's controller).
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

    /**
     * This record with the trigger's filter and "one or more creatures" taken in: the form the legacy view gives an
     * ability whose effect is this record.
     */
    public AllyCombatDamageTriggerEffect withTriggerOptions(PermanentPredicate filter, boolean oneOrMore) {
        if (filter == null && !oneOrMore) {
            return this;
        }
        if (filter != null && dealerPredicate != null) {
            throw new IllegalStateException("The dealer filter is the trigger's or this record's, not both: " + this);
        }
        return new AllyCombatDamageTriggerEffect(filter != null ? filter : dealerPredicate, effect,
                bindSourceToDealer, oncePerDamageStep || oneOrMore, opponentDealtDamage);
    }

    /** This record around {@code effect} in place of its effect. */
    public AllyCombatDamageTriggerEffect withEffect(CardEffect effect) {
        return new AllyCombatDamageTriggerEffect(dealerPredicate, effect, bindSourceToDealer, oncePerDamageStep,
                opponentDealtDamage);
    }

    /** Compatibility accessor for the graveyard-trigger terminology. */
    public boolean oneOrMoreDealers() {
        return oncePerDamageStep;
    }
}
