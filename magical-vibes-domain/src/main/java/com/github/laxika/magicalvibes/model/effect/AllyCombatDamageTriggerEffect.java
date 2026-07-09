package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Marker effect for the ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER slot.
 * When a creature controlled by the same player deals combat damage to a player and matches
 * {@code dealerPredicate} (null = any creature), the wrapped {@code effect} is put on the stack
 * for the source's controller. Wrap {@code effect} in a {@link MayEffect} for "you may" wordings.
 *
 * <p>{@code bindSourceToDealer} controls the stack entry's source permanent: {@code false} binds
 * it to the permanent carrying this trigger (Boggart Mob: "Whenever a Goblin you control deals
 * combat damage to a player, you may create a 1/1 black Goblin Rogue creature token."),
 * {@code true} binds it to the creature that dealt the damage, so source-referencing effects like
 * {@link PutCountersOnSourceEffect} apply to the dealer (Rakish Heir: "Whenever a Vampire you
 * control deals combat damage to a player, put a +1/+1 counter on it.").
 */
public record AllyCombatDamageTriggerEffect(PermanentPredicate dealerPredicate, CardEffect effect,
                                            boolean bindSourceToDealer) implements CardEffect {

    public AllyCombatDamageTriggerEffect(PermanentPredicate dealerPredicate, CardEffect effect) {
        this(dealerPredicate, effect, false);
    }
}
