package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.StackEntry;

/**
 * Creates a copy of the spell that caused the resolving spell-cast trigger. Unless
 * {@code mayChooseNewTargets} is cleared (a plain "copy it" with no "you may choose new targets"),
 * the copy's controller is asked whether to choose new targets for it.
 */
public record CopyTriggeringSpellEffect(boolean tokenCopy, boolean retargetToSource, StackEntry spellSnapshot,
                                        boolean mayChooseNewTargets)
        implements TriggeringSpellReferencingEffect {

    public CopyTriggeringSpellEffect() {
        this(false, false, null, true);
    }

    public CopyTriggeringSpellEffect(boolean tokenCopy) {
        this(tokenCopy, false, null, true);
    }

    public CopyTriggeringSpellEffect(boolean tokenCopy, boolean retargetToSource) {
        this(tokenCopy, retargetToSource, null, true);
    }

    public CopyTriggeringSpellEffect(boolean tokenCopy, boolean retargetToSource, StackEntry spellSnapshot) {
        this(tokenCopy, retargetToSource, spellSnapshot, true);
    }

    /** Creates a copy that is controlled by the trigger's controller and targets its source. */
    public static CopyTriggeringSpellEffect targetingSource() {
        return new CopyTriggeringSpellEffect(false, true, null, true);
    }

    /** The same copy effect, with the copy keeping the copied spell's targets (no new-target choice). */
    public CopyTriggeringSpellEffect withoutNewTargetChoice() {
        return new CopyTriggeringSpellEffect(tokenCopy, retargetToSource, spellSnapshot, false);
    }
}
