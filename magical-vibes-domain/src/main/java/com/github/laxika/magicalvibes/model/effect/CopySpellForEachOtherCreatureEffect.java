package com.github.laxika.magicalvibes.model.effect;

/**
 * "If that spell targets only this creature, copy the spell for each other creature that spell could target. Each copy
 * targets a different one of those creatures" (Ink-Treader Nephilim): the effect of a filtered cast trigger whose
 * intervening "if" is its "targets only this creature" stack-entry filter. As it resolves it reads the stack entry's
 * triggering spell (the spell while it is still on the stack, else as it last existed there, CR 608.2h), checks the
 * intervening "if" again (CR 603.4: no copy if the spell no longer targets only the source), and makes the copies under
 * the ability's controller, each other creature judged as the spell's caster would target it.
 */
public record CopySpellForEachOtherCreatureEffect() implements CardEffect {
}
