package com.github.laxika.magicalvibes.model.effect;

/**
 * Sacrifices the stack entry's source permanent. The default form requires the ability's
 * controller to still control that permanent; the flagged form instructs its current controller
 * to sacrifice it instead.
 */
public record SacrificeSelfEffect(boolean currentControllerSacrifices)
        implements CombatDamageTriggerContextEffect {

    public SacrificeSelfEffect() {
        this(false);
    }

    @Override
    public TargetSpec targetSpec() {
        // Implicitly acts on its own source permanent — so trigger collectors that key off
        // selfTargeting() (e.g. the spell-cast collector) carry the source id onto the stack entry.
        return new TargetSpec(null, false, null, true, 1);
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        // "…and sacrifice this creature" on a combat-damage trigger needs the source bound so the
        // stack entry knows which permanent to sacrifice (e.g. Kathari Bomber).
        return TriggerContext.SOURCE_SELF;
    }
}
