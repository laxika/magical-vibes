package com.github.laxika.magicalvibes.model.effect;

public record ChangeTargetOfTargetSpellToSourceEffect() implements CardEffect {
    @Override public boolean canTargetSpell() { return true; }
}
