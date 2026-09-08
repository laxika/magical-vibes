package com.github.laxika.magicalvibes.model.effect;

/**
 * Schedule a permanent to be sacrificed at the beginning of the next end step. The plain form
 * schedules the source (e.g. Brackwater Elemental's "When this creature attacks or blocks,
 * sacrifice it at the beginning of the next end step"), while {@link #forAttachedPermanent()}
 * schedules the permanent attached to the source Aura or Equipment. Neither form targets the
 * permanent. Sacrifice, not destruction (ignores indestructible/regeneration).
 */
public record SacrificeSelfAtEndStepEffect(boolean attachedPermanent, boolean currentControllerSacrifices)
        implements AttachedPermanentSelfTargetingEffect {

    public SacrificeSelfAtEndStepEffect() {
        this(false, false);
    }

    /** Schedules the permanent attached to the source Equipment or Aura instead of the source. */
    public static SacrificeSelfAtEndStepEffect forAttachedPermanent() {
        return new SacrificeSelfAtEndStepEffect(true, false);
    }

    /** Schedules the source and instructs whoever controls it at resolution to sacrifice it. */
    public static SacrificeSelfAtEndStepEffect byCurrentController() {
        return new SacrificeSelfAtEndStepEffect(false, true);
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
