package com.github.laxika.magicalvibes.model.effect;

/**
 * Sacrifices the stack entry's source permanent. The default form requires the ability's
 * controller to still control that permanent; the flagged form instructs its current controller
 * to sacrifice it instead. An optional token rider is created for that player only when the
 * sacrifice succeeds.
 */
public record SacrificeSelfEffect(boolean currentControllerSacrifices,
                                  CreateTokenEffect tokenForSacrificingPlayer)
        implements CardEffect {

    public SacrificeSelfEffect() {
        this(false, null);
    }

    public SacrificeSelfEffect(boolean currentControllerSacrifices) {
        this(currentControllerSacrifices, null);
    }

    @Override
    public TargetSpec targetSpec() {
        // Implicitly acts on its own source permanent — so trigger collectors that key off
        // selfTargeting() (e.g. the spell-cast collector) carry the source id onto the stack entry.
        return new TargetSpec(null, false, null, true, 1);
    }
}
