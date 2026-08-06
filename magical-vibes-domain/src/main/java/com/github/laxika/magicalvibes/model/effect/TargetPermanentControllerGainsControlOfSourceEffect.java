package com.github.laxika.magicalvibes.model.effect;

/**
 * "That permanent's controller gains control of this permanent." The controller of the ability's
 * targeted permanent — which may be the ability's own controller — gains control of the
 * <em>source</em> permanent for {@code duration}. Non-targeting itself: it reads the target chosen
 * by a sibling effect on the same ability (Starke of Rath pairs it with
 * {@link DestroyTargetPermanentEffect}).
 *
 * <p>List it <em>before</em> the effect that removes the target, so the target's controller is still
 * readable at resolution; both effects resolve in the same pass, so the ordering is unobservable.
 *
 * <p>Differs from {@link OpponentGainsControlOfSourceCreatureEffect}, which always hands the source
 * to an opponent regardless of any target.
 *
 * @param duration how long the control change lasts
 */
public record TargetPermanentControllerGainsControlOfSourceEffect(ControlDuration duration)
        implements ControlStealingEffect {

    @Override
    public ControlDuration controlDuration() {
        return duration;
    }
}
