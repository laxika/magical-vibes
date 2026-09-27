package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Mass "can't be blocked this turn" — every creature on the battlefield (Venser, the Sojourner), or
 * only the resolving controller's creatures when {@code controllerOnly} is set (Glaring Spotlight).
 * When {@code filter} is non-null, only matching creatures are affected.
 *
 * @param controllerOnly whether the restriction is limited to creatures the controller controls
 * @param filter         optional filter limiting the affected creatures
 */
public record MakeAllCreaturesUnblockableEffect(boolean controllerOnly, PermanentPredicate filter)
        implements CardEffect {

    /** Every creature on every battlefield can't be blocked this turn. */
    public MakeAllCreaturesUnblockableEffect() {
        this(false, null);
    }

    public MakeAllCreaturesUnblockableEffect(boolean controllerOnly) {
        this(controllerOnly, null);
    }

    /** Only the controller's creatures can't be blocked this turn. */
    public static MakeAllCreaturesUnblockableEffect ownCreatures() {
        return new MakeAllCreaturesUnblockableEffect(true, null);
    }

    /** Matching creatures on every battlefield can't be blocked this turn. */
    public static MakeAllCreaturesUnblockableEffect matching(PermanentPredicate filter) {
        return new MakeAllCreaturesUnblockableEffect(false, filter);
    }
}
