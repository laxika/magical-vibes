package com.github.laxika.magicalvibes.model.effect;

/**
 * "Put a +1/+1 counter on this creature if one of that creature's creature types is on your buddy list. Otherwise,
 * write one of its creature types on your buddy list" (Champion of the Hareish): an effect "if" on the triggering
 * creature, read as the ability resolves from the creature as it is then, or as it last existed on the battlefield
 * once it has left (CR 608.2h). The stack entry's triggering permanent is the creature.
 */
public record BuddyListGrowOrWriteEffect() implements CardEffect {
}
