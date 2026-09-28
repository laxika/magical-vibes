package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the source Aura and its enchanted creature, then returns both at the beginning of the
 * enchanted creature controller's next declare-attackers step with the creature tapped and
 * attacking and the Aura attached to it.
 */
public record ExileEnchantedCreatureAndSelfReturnAtNextTurnDeclareAttackersEffect()
        implements CardEffect {
}
