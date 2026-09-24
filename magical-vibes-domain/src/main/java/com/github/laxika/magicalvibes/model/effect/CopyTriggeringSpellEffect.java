package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.StackEntry;

/** Creates a copy of the spell that caused the resolving spell-cast trigger. */
public record CopyTriggeringSpellEffect(boolean tokenCopy, boolean retargetToSource, StackEntry spellSnapshot)
        implements TriggeringSpellReferencingEffect {

    public CopyTriggeringSpellEffect() {
        this(false, false, null);
    }

    public CopyTriggeringSpellEffect(boolean tokenCopy) {
        this(tokenCopy, false, null);
    }

    public CopyTriggeringSpellEffect(boolean tokenCopy, boolean retargetToSource) {
        this(tokenCopy, retargetToSource, null);
    }

    /** Creates a copy that is controlled by the trigger's controller and targets its source. */
    public static CopyTriggeringSpellEffect targetingSource() {
        return new CopyTriggeringSpellEffect(false, true, null);
    }
}
