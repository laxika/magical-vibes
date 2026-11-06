package com.github.laxika.magicalvibes.model.effect;

/**
 * Creates a copy of the spell that caused the resolving spell-cast trigger: the spell still on the stack, or, once it
 * has left, the spell as it was when the ability triggered (the stack entry's triggering spell, CR 608.2h). Unless
 * {@code mayChooseNewTargets} is cleared (a plain "copy it" with no "you may choose new targets"), the copy's
 * controller is asked whether to choose new targets for it.
 */
public record CopyTriggeringSpellEffect(boolean tokenCopy, boolean retargetToSource, boolean mayChooseNewTargets)
        implements TriggeringSpellReferencingEffect {

    public CopyTriggeringSpellEffect() {
        this(false, false, true);
    }

    public CopyTriggeringSpellEffect(boolean tokenCopy) {
        this(tokenCopy, false, true);
    }

    public CopyTriggeringSpellEffect(boolean tokenCopy, boolean retargetToSource) {
        this(tokenCopy, retargetToSource, true);
    }

    /** Creates a copy that is controlled by the trigger's controller and targets its source. */
    public static CopyTriggeringSpellEffect targetingSource() {
        return new CopyTriggeringSpellEffect(false, true, true);
    }

    /** The same copy effect, with the copy keeping the copied spell's targets (no new-target choice). */
    public CopyTriggeringSpellEffect withoutNewTargetChoice() {
        return new CopyTriggeringSpellEffect(tokenCopy, retargetToSource, false);
    }
}
