package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.XValue;

/**
 * "Prevent the next N damage that would be dealt to [you / you and/or permanents you control / target creature] this
 * turn. If damage is prevented this way, [this] deals that much damage to [target]": one prevention shield
 * ({@code DamageRedirectShield}) whose prevented damage the ability's source deals to the redirect target.
 *
 * <p>"Permanents you control" is judged when the damage would be dealt, as a prevention shield watches for it
 * (CR 615.7), not fixed as the effect resolves.
 *
 * @param amount                 how much damage the shield prevents, evaluated once as it resolves
 * @param protectedPermanent     the target creature it protects ({@link Subjects#target(int)}), or {@code null} when
 *                               it protects its controller
 * @param andPermanentsYouControl with a {@code null} {@code protectedPermanent}: the shield also covers the
 *                               permanents its controller controls (Divine Deflection)
 * @param redirectTarget         which targets may be dealt the prevented damage
 * @param redirectTargetGroup    the target group of the redirect target, or {@code null} for the entry's target
 */
public record PreventNextDamageAndRedirectEffect(
        DynamicAmount amount,
        PermanentSubject protectedPermanent,
        boolean andPermanentsYouControl,
        TargetPredicate redirectTarget,
        Integer redirectTargetGroup
) implements CardEffect {

    public PreventNextDamageAndRedirectEffect {
        if (protectedPermanent != null && andPermanentsYouControl) {
            throw new IllegalArgumentException("A shield on a target creature covers no other permanents");
        }
    }

    /** "Prevent the next X damage that would be dealt to you this turn. ... deals that much damage to target …" */
    public static PreventNextDamageAndRedirectEffect toYouX(TargetPredicate redirectTarget) {
        return new PreventNextDamageAndRedirectEffect(new XValue(), null, false, redirectTarget, null);
    }

    /** "Prevent the next X damage that would be dealt to you and/or permanents you control … to any target." */
    public static PreventNextDamageAndRedirectEffect toYouAndYourPermanentsX() {
        return new PreventNextDamageAndRedirectEffect(new XValue(), null, true, TargetPredicates.anyTarget(), null);
    }

    /**
     * "Prevent the next N damage that would be dealt to target creature this turn. ... to any target": the creature is
     * target group 0, the redirect target group 1.
     */
    public static PreventNextDamageAndRedirectEffect toTargetCreature(DynamicAmount amount) {
        return new PreventNextDamageAndRedirectEffect(amount, Subjects.target(0), false, TargetPredicates.anyTarget(), 1);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(redirectTarget);
    }
}
