package com.github.laxika.magicalvibes.model.effect;

/**
 * Next of Kin's death trigger: optionally put a creature with lesser mana value than the creature that died from hand
 * or the command zone onto the battlefield, then return the source Aura attached to it at the next end step. The
 * dying creature's mana value is the stack entry's, as it last existed on the battlefield (CR 603.10a).
 */
public record PutLesserManaValueCreatureFromHandOrCommandZoneAndReturnAuraEffect() implements CardEffect {
}
