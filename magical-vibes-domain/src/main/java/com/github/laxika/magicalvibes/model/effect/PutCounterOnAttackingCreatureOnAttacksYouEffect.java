package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Marker for a player-attached Aura's "whenever a creature attacks you" counter trigger.
 * Combat attack collection converts it to a non-targeting counter effect for the captured attacker.
 */
public record PutCounterOnAttackingCreatureOnAttacksYouEffect(CounterType counterType)
        implements CardEffect {
}
