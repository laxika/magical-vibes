package com.github.laxika.magicalvibes.model.effect;

/**
 * Changes every target occurrence of a target spell to the source permanent when all of the
 * spell's target occurrences currently identify one creature.
 */
public record ChangeAllTargetsOfTargetSpellToSourceEffect() implements CardEffect {
    @Override public TargetSpec targetSpec() { return TargetSpec.benign(TargetPredicates.spellOnStack()); }
}
