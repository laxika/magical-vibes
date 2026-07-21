package com.github.laxika.magicalvibes.model.effect;

/**
 * Return target spell to its owner's hand. This is <em>not</em> countering — uncounterable spells
 * can still be returned (Hullbreaker Horror). Copies cease to exist when removed from the stack.
 */
public record ReturnTargetSpellToHandEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.SPELL_ON_STACK);
    }
}
