package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.replacement.ReplacementOption;

/**
 * One replacement or prevention effect that can apply to damage dealt to a permanent, as
 * {@link DamagePreventionService#creatureDamageSteps()} lists them. The fixed-order chain that
 * combat and redirected damage still use walks the steps in order; the CR 616.1 ordering offers
 * each applicable step to the permanent's controller.
 *
 * @param family     identifies the step for CR 614.5 bookkeeping
 * @param label      what the controller sees when choosing
 * @param kind       how the step modifies the damage
 * @param pure       whether the step only changes the amount (nothing consumed, no rider)
 * @param applies    whether the step would modify {@code damage} now, without changing the game
 * @param apply      applies the step and returns the damage left
 * @param endsChain  whether the fixed-order chain stops after this step applies, keeping what it
 *                   returned (counter-removal replacements for damage that can't be prevented)
 */
public record CreatureDamageStep(String family, String label, ReplacementOption.Kind kind, boolean pure,
                                 Applies applies, Apply apply, Applies endsChain) {

    /** Who the damage is dealt to and how, for the steps. */
    public record Context(Permanent permanent, boolean combatDamage, Permanent damageSource,
                          boolean applySelfDamagePrevention) {}

    @FunctionalInterface
    public interface Applies {
        boolean test(GameData gameData, Context context, int damage);
    }

    @FunctionalInterface
    public interface Apply {
        int apply(GameData gameData, Context context, int damage);
    }
}
