package com.github.laxika.magicalvibes.model.effect;

/**
 * "[Player] may choose new targets for [spell]", always inside a {@code MayEffect} whose chooser is that player
 * ({@code CONTROLLER}, or {@code TARGET_SPELL_CONTROLLER} for "the spell's controller"). The player may leave any
 * target unchanged; a new target must be legal (CR 115.7d). By default the "may" is asked also for a spell with no
 * targets or one that has left the stack, and accepting then changes nothing (Redirect, Deflecting Swat).
 *
 * @param mustChangeAllTargets "change the target of target spell" (Goblin Flectomancer): every target must change
 * @param spell                {@code Subjects.targetSpell()} (declares the benign spell target; Redirect, Commandeer,
 *                             Sudden Substitution) or {@code Subjects.triggeringSpell()} ("that spell" of a spell-cast
 *                             trigger, declares none; Perplexing Chimera)
 * @param onlyIfItHasTargets   whether nothing is asked when the spell has left the stack or has no targets: the
 *                             "you may choose new targets for it" after a control change (Commandeer, Aethersnatch,
 *                             Sudden Substitution, Perplexing Chimera)
 */
public record ChooseNewTargetsForTargetSpellEffect(boolean mustChangeAllTargets, PermanentSubject spell,
                                                   boolean onlyIfItHasTargets)
        implements CardEffect {

    public ChooseNewTargetsForTargetSpellEffect {
        if (!(spell instanceof PermanentSubject.TargetSpell) && !(spell instanceof PermanentSubject.TriggeringSpell)) {
            throw new IllegalArgumentException("New targets are chosen for a target spell or the triggering spell: "
                    + spell);
        }
    }

    public ChooseNewTargetsForTargetSpellEffect(boolean mustChangeAllTargets) {
        this(mustChangeAllTargets, Subjects.targetSpell(), false);
    }

    public ChooseNewTargetsForTargetSpellEffect() {
        this(false);
    }

    /**
     * "You may choose new targets for [spell]" after gaining or exchanging control of it: asked only while the spell
     * is on the stack with targets.
     */
    public static ChooseNewTargetsForTargetSpellEffect ifItHasTargets(PermanentSubject spell) {
        return new ChooseNewTargetsForTargetSpellEffect(false, spell, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return spell instanceof PermanentSubject.TargetSpell
                ? TargetSpec.benign(TargetPredicates.spellOnStack()) : TargetSpec.NONE;
    }
}
