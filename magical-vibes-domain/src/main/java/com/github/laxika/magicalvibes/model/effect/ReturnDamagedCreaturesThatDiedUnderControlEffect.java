package com.github.laxika.magicalvibes.model.effect;

/**
 * End-step effect for Krovikan Vampire: put each creature card dealt damage by this creature this
 * turn that died (and is still continuously in a graveyard) onto the battlefield under your
 * control. Returned creatures are linked to the source for the control-loss sacrifice (same
 * linkage as Seraph via {@code GameData.seraphReturnedCreatures}).
 */
public record ReturnDamagedCreaturesThatDiedUnderControlEffect() implements ControlStealingEffect {

    @Override
    public ControlDuration controlDuration() {
        return ControlDuration.PERMANENT;
    }
}
