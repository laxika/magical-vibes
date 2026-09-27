package com.github.laxika.magicalvibes.model.effect;

/**
 * Chooses one opponent of the effect controller at random and makes the source creature attack
 * that player during this combat if able.
 *
 * @param excludeOpponentsAttackedLastCombat whether to exclude opponents this creature attacked
 *                                           during its immediately preceding combat
 * @param tapIfNoOpponent whether to tap the source when no opponent remains eligible
 */
public record ChooseRandomOpponentMustAttackThisCombatEffect(
        boolean excludeOpponentsAttackedLastCombat,
        boolean tapIfNoOpponent
) implements CardEffect {

    public ChooseRandomOpponentMustAttackThisCombatEffect() {
        this(false, false);
    }
}
