package com.github.laxika.magicalvibes.model.effect;

/**
 * Gains control of the permanent the source Aura is attached to for the given
 * {@link ControlDuration}. Non-targeting: the affected permanent is re-derived at resolution from
 * the Aura's {@code attachedTo}, so it works on slots that carry no target of their own
 * ({@code UPKEEP_TRIGGERED} on an Aura, Wellspring).
 *
 * <p>Distinct from {@link GainControlOfEnchantedTargetEffect}, which is the activated-ability
 * variant reading the entry's target and holding control for as long as that permanent remains
 * enchanted (Rootwater Matriarch).
 *
 * @param duration how long control is retained
 */
public record GainControlOfEnchantedPermanentEffect(ControlDuration duration)
        implements ControlStealingEffect {

    @Override
    public ControlDuration controlDuration() {
        return duration;
    }
}
