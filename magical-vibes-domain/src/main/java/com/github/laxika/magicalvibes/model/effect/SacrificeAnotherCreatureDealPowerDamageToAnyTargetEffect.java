package com.github.laxika.magicalvibes.model.effect;

/**
 * "You may sacrifice another creature. When you do, this creature deals damage equal to that
 * creature's power to any target." (Heart-Piercer Manticore's enter trigger.)
 *
 * <p>Placed inside a {@link MayEffect} on {@code ON_ENTER_BATTLEFIELD}. The any-target is chosen as
 * the trigger resolves (CR 603.5 resolution-time "may"): the controller is first asked whether to
 * sacrifice, then — on acceptance — chooses the target and, when they control another creature,
 * which creature to sacrifice. The sacrificed creature's effective power (clamped to 0 per CR
 * 510.1a) is captured before it leaves the battlefield and the source permanent deals that much
 * damage to the chosen target. Declining, or controlling no other creature, deals no damage.
 *
 * <p>Targeting is declared as {@link TargetCategory#ANY_TARGET}; the resolution-time may-ability
 * pipeline reads {@code targetSpec()} to offer creatures and players (the same any-target scope as
 * every other resolution-time "may" ability).
 */
public record SacrificeAnotherCreatureDealPowerDamageToAnyTargetEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.ANY_TARGET);
    }
}
