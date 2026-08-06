package com.github.laxika.magicalvibes.model.effect;

/**
 * Mass "can't be blocked this turn" — every creature on the battlefield (Venser, the Sojourner), or
 * only the resolving controller's creatures when {@code controllerOnly} is set (Glaring Spotlight).
 *
 * @param controllerOnly whether the restriction is limited to creatures the controller controls
 */
public record MakeAllCreaturesUnblockableEffect(boolean controllerOnly) implements CardEffect {

    /** Every creature on every battlefield can't be blocked this turn. */
    public MakeAllCreaturesUnblockableEffect() {
        this(false);
    }

    /** Only the controller's creatures can't be blocked this turn. */
    public static MakeAllCreaturesUnblockableEffect ownCreatures() {
        return new MakeAllCreaturesUnblockableEffect(true);
    }
}
