package com.github.laxika.magicalvibes.model.effect;

/**
 * "You may choose any number of other creatures that spell could target and pay {@code manaCost} for each of those
 * creatures. If you do, for each of those creatures, copy that spell. The copy targets that creature" (Feather, Radiant
 * Arbiter): the effect of a filtered cast trigger ("a noncreature spell that targets only this creature"). As it
 * resolves it reads the stack entry's triggering spell (the spell while it is still on the stack, else as it last
 * existed there, CR 608.2h); "other" is other than the ability's source, and a copy of a permanent spell becomes a
 * token.
 *
 * @param manaCost the cost paid for each chosen creature
 */
public record CopySpellForEachOtherCreatureWithManaEffect(String manaCost) implements CardEffect {
}
