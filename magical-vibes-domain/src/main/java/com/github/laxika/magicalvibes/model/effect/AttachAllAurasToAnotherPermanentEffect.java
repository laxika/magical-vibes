package com.github.laxika.magicalvibes.model.effect;

/**
 * "Attach all Auras enchanting target permanent to another permanent with the same controller"
 * (Glamer Spinners). On resolution the controller chooses another permanent — one controlled by the
 * same player as the targeted permanent that every Aura currently on the target can legally enchant —
 * and all of those Auras move onto it (CR 613.7e gives each a new timestamp). If the target has no
 * Auras, or no single permanent can receive all of them, nothing happens.
 */
public record AttachAllAurasToAnotherPermanentEffect() implements CardEffect {

    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PERMANENT);
    }
}
