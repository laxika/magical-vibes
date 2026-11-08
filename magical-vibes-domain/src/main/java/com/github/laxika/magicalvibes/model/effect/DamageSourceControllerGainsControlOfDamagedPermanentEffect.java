package com.github.laxika.magicalvibes.model.effect;

/**
 * "That source's controller gains control of this creature" (Crag Saurian, Contested War Zone): the controller of the
 * stack entry's triggering permanent as the ability resolves, or its last known controller (CR 608.2h), or the
 * controller the entry recorded for a source that was never a permanent.
 *
 * @param attackingPlayerGainsControl whether the attacking player gains control instead ({@link #attackingPlayer()}:
 *                                    Contested Game Ball's "the attacking player gains control of this artifact"),
 *                                    read as the ability resolves: the active player (CR 506.2)
 */
public record DamageSourceControllerGainsControlOfDamagedPermanentEffect(boolean attackingPlayerGainsControl)
        implements CardEffect {

    public DamageSourceControllerGainsControlOfDamagedPermanentEffect() {
        this(false);
    }

    /** "The attacking player gains control of this permanent". */
    public static DamageSourceControllerGainsControlOfDamagedPermanentEffect attackingPlayer() {
        return new DamageSourceControllerGainsControlOfDamagedPermanentEffect(true);
    }
}
