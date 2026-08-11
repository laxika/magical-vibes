package com.github.laxika.magicalvibes.model.effect;

/**
 * Prevents all damage dealt by the targeted instant or sorcery spell for the rest of the turn.
 */
public record PreventDamageFromTargetSpellEffect(boolean gainLife, boolean instantSorceryOnly) implements CardEffect {

    public PreventDamageFromTargetSpellEffect() {
        this(false, true);
    }

    /** Hallow: the controller gains life equal to damage prevented by this effect. */
    public static PreventDamageFromTargetSpellEffect withLifeGain() {
        return new PreventDamageFromTargetSpellEffect(true, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }
}
