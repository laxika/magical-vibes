package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller of the spell or ability that caused this trigger gains control of the permanent
 * enchanted by the source Aura.
 *
 * <p>The triggering stack entry is carried as the trigger's stack target, while the enchanted
 * permanent is re-derived from the source Aura at resolution.
 *
 * @param duration how long control is retained
 */
public record TriggeringObjectControllerGainsControlOfEnchantedPermanentEffect(ControlDuration duration)
        implements ControlStealingEffect, TriggeringSpellReferencingEffect {

    @Override
    public ControlDuration controlDuration() {
        return duration;
    }
}
