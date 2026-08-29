package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Registers a delayed triggered ability watching this effect's target creatures:
 * "Until your next turn, whenever either of those creatures deals combat damage, &lt;effects&gt;."
 * (Tamiyo, Field Researcher's +1, with {@code DrawCardEffect(1)}.)
 *
 * <p>The watch lives on {@code GameData.delayedActions} as a
 * {@code DelayedWatchedCreaturesCombatDamage} and is controlled by the player who resolved this
 * effect, so the granted ability resolves for them even when a watched creature is (or becomes) an
 * opponent's. It expires at the beginning of that player's next turn, not at end of turn.
 *
 * @param effects the effects the delayed ability resolves each time a watched creature deals
 *                combat damage
 * @param combatDamageToPlayerOnly whether the delayed ability only triggers when the watched
 *                                 creature deals combat damage to a player
 * @param untilEndOfTurn whether the delayed ability expires during this turn's cleanup step;
 *                       otherwise it expires at the controller's next turn
 */
public record RegisterDelayedWatchedCreaturesCombatDamageEffect(
        List<CardEffect> effects,
        boolean combatDamageToPlayerOnly,
        boolean untilEndOfTurn
) implements CardEffect {

    public RegisterDelayedWatchedCreaturesCombatDamageEffect(List<CardEffect> effects) {
        this(effects, false, false);
    }

    public RegisterDelayedWatchedCreaturesCombatDamageEffect(List<CardEffect> effects,
                                                              boolean combatDamageToPlayerOnly) {
        this(effects, combatDamageToPlayerOnly, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
