package com.github.laxika.magicalvibes.model.effect;

/**
 * Changes the triggering spell's only target to the creature enchanted by the source Aura when
 * that creature is a legal target.
 */
public record ChangeTargetOfTargetSpellToEnchantedCreatureEffect()
        implements AttachedPermanentSelfTargetingEffect {

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(null, false, null, true, 1);
    }
}
