package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "Change a target of [spell] to [new target]": one target of the spell is changed to the new target if it is
 * legal for the spell (CR 115.7, 115.7d), the controller choosing which target when the spell has several.
 *
 * @param spell     {@code Subjects.targetSpell()} ("target spell or ability", declares the benign spell target) or
 *                  {@code Subjects.triggeringSpell()} ("whenever an opponent casts a spell, … change the target",
 *                  declares none)
 * @param newTarget the permanent the target becomes: {@code source()} ("to this creature", Spellskite) or
 *                  {@code attachedCreature()} ("to enchanted creature if able", Captured by the Consulate)
 */
public record ChangeTargetOfTargetSpellToSourceEffect(PermanentSubject spell, PermanentSubject newTarget)
        implements CardEffect {

    public ChangeTargetOfTargetSpellToSourceEffect {
        Objects.requireNonNull(newTarget, "newTarget");
        if (!(spell instanceof PermanentSubject.TargetSpell) && !(spell instanceof PermanentSubject.TriggeringSpell)) {
            throw new IllegalArgumentException("Changes a target of a target spell or the triggering spell: " + spell);
        }
    }

    /** "Change a target of target spell or ability to this creature". */
    public ChangeTargetOfTargetSpellToSourceEffect() {
        this(Subjects.targetSpell(), Subjects.source());
    }

    /** "Whenever an opponent casts a spell, if it has a single target, change the target to enchanted creature if able". */
    public static ChangeTargetOfTargetSpellToSourceEffect triggeringSpellToEnchantedCreature() {
        return new ChangeTargetOfTargetSpellToSourceEffect(Subjects.triggeringSpell(), Subjects.attachedCreature());
    }

    @Override
    public TargetSpec targetSpec() {
        return spell instanceof PermanentSubject.TargetSpell
                ? TargetSpec.benign(TargetPredicates.spellOnStack()) : TargetSpec.NONE;
    }
}
