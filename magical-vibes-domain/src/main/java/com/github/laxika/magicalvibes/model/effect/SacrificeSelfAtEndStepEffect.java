package com.github.laxika.magicalvibes.model.effect;

/**
 * Schedule a permanent to be sacrificed at the beginning of the next end step. The plain form
 * schedules the source (e.g. Brackwater Elemental's "When this creature attacks or blocks,
 * sacrifice it at the beginning of the next end step"), while {@link #forAttachedPermanent()}
 * schedules the permanent attached to the source Aura or Equipment. Neither form targets the
 * permanent. Sacrifice, not destruction (ignores indestructible/regeneration).
 */
public record SacrificeSelfAtEndStepEffect(
        boolean attachedPermanent, boolean currentControllerSacrifices, boolean currentControllersEndStepOnly)
        implements AttachedPermanentSelfTargetingEffect {

    public SacrificeSelfAtEndStepEffect() {
        this(false, false, false);
    }

    /** Schedules the permanent attached to the source Equipment or Aura instead of the source. */
    public static SacrificeSelfAtEndStepEffect forAttachedPermanent() {
        return new SacrificeSelfAtEndStepEffect(true, false, false);
    }

    /** Schedules the source and instructs whoever controls it at resolution to sacrifice it. */
    public static SacrificeSelfAtEndStepEffect byCurrentController() {
        return new SacrificeSelfAtEndStepEffect(false, true, false);
    }

    /**
     * Schedules the source for "at the beginning of your end step": the sacrifice waits for an end step of
     * the permanent's current controller, who then sacrifices it (Terminal Velocity, Esper Terra).
     */
    public static SacrificeSelfAtEndStepEffect atCurrentControllersEndStep() {
        return new SacrificeSelfAtEndStepEffect(false, true, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return attachedPermanent
                ? new TargetSpec(null, false, null, true, 1)
                : TargetSpec.NONE;
    }

    @Override
    public boolean resolvesAgainstAttachedPermanent() {
        return attachedPermanent;
    }
}
