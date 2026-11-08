package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * "That player copies that spell for each other [subtype] that spell could target. Each copy targets a different one of
 * those [subtype]s" (Precursor Golem): the effect of a filtered cast trigger whose stack-entry filter is "targets only a
 * single [subtype]". As it resolves it reads the stack entry's triggering spell (the spell while it is still on the
 * stack, else as it last existed there, CR 608.2h): the spell's controller controls the copies, and "other" is other
 * than the permanent the spell targeted as the ability triggered.
 *
 * @param subtype the subtype of the permanents the copies target
 */
public record CopySpellForEachOtherSubtypePermanentEffect(CardSubtype subtype) implements CardEffect {
}
