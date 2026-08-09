package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.UUID;

/**
 * The controller of the targeted permanent gains control of the Equipment that granted the ability.
 * The Equipment id and the excluded source-subtype result are bound when the ability is activated.
 *
 * @param duration control duration
 * @param excludedSourceSubtype subtype that prevents the control change, or {@code null}
 * @param equipmentId granting Equipment id, or {@code null} on the card definition
 * @param sourceHadExcludedSubtype whether the activating creature had the excluded subtype
 */
public record TargetPermanentControllerGainsControlOfGrantingEquipmentEffect(
        ControlDuration duration,
        CardSubtype excludedSourceSubtype,
        UUID equipmentId,
        boolean sourceHadExcludedSubtype
) implements ControlStealingEffect {

    public TargetPermanentControllerGainsControlOfGrantingEquipmentEffect(
            ControlDuration duration, CardSubtype excludedSourceSubtype) {
        this(duration, excludedSourceSubtype, null, false);
    }

    @Override
    public ControlDuration controlDuration() {
        return duration;
    }
}
