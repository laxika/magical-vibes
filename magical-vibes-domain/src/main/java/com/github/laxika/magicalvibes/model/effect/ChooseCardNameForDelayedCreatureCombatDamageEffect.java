package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Asks the controller to choose a card name, then registers the supplied effects to trigger when
 * a creature with that name deals combat damage to a player this turn.
 */
public record ChooseCardNameForDelayedCreatureCombatDamageEffect(
        List<CardEffect> effects,
        boolean combatDamageToPlayerOnly,
        boolean untilEndOfTurn
) implements CardEffect {

    public ChooseCardNameForDelayedCreatureCombatDamageEffect(List<CardEffect> effects) {
        this(effects, true, true);
    }

    public ChooseCardNameForDelayedCreatureCombatDamageEffect {
        effects = List.copyOf(effects);
    }
}
