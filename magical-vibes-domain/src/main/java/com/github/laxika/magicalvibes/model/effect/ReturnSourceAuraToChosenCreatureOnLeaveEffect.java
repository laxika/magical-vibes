package com.github.laxika.magicalvibes.model.effect;

/**
 * Traveling Plague's "when enchanted creature leaves the battlefield, that creature's controller returns this Aura
 * from its owner's graveyard to the battlefield": that player chooses a legal creature for it to enchant and the Aura
 * enters under their control. "That creature's controller" is the controller of the stack entry's triggering
 * permanent, as it last existed on the battlefield (CR 603.10a), read as the ability resolves.
 */
public record ReturnSourceAuraToChosenCreatureOnLeaveEffect() implements CardEffect {
}
