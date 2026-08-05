package com.github.laxika.magicalvibes.model.effect;

public record ChangeTargetOfTargetSpellToSourceEffect() implements CardEffect {
    @Override public TargetSpec targetSpec() { return TargetSpec.benign(TargetPredicates.spellOnStack()); }
}
