package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability interface for static effects that multiply <em>all</em> damage dealt in the game,
 * regardless of source, target, or controller (Furnace of Rath and similar). Lets consumers ask
 * "does this permanent globally scale damage, and by how much" without naming the concrete record.
 *
 * <p>Deliberately narrow: this is the <em>global, unconditional</em> damage multiplier only. The
 * selective doublers — per-controller ({@code DoubleControllerDamageEffect}), per-enchanted-player
 * ({@code DoubleDamageToEnchantedPlayerEffect}), and per-equipped-creature
 * ({@code DoubleEquippedCreatureCombatDamageEffect}) — each pick out a different subset of
 * permanents and therefore do <em>not</em> implement this interface; a shared marker would make the
 * global multiplier method wrongly count them.
 *
 * <p>Descriptive only: {@link #damageMultiplierFactor()} states a fact intrinsic to the effect,
 * never a score.
 */
public interface GlobalDamageMultiplyingEffect extends CardEffect {

    /**
     * The factor by which this effect multiplies every point of damage dealt (2 for a plain
     * doubler). Multiple such effects stack multiplicatively.
     */
    int damageMultiplierFactor();
}
