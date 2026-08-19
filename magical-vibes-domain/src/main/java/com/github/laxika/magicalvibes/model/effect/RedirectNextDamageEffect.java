package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "The next {@code amount} damage that would be dealt to X this turn is dealt to Y instead," or
 * the next damage event is redirected in full when {@code nextEvent} is true.
 * Both ends of the redirection are roles rather than fixed objects, which is the only axis the
 * whole family varies on: {@code protectedRole} is whose incoming damage is diverted and
 * {@code destinationRole} is who receives it instead. The shield matches damage from any source
 * (combat or noncombat) and is consumed once {@code amount} damage has been redirected.
 *
 * <p>A permanent-protecting instance installs an amount-limited or next-event
 * {@code CreatureDamageRedirectShield}; a player-protecting one (reachable only when
 * {@code protectedRole} resolves to a player, as with Martyrdom's any-target grant) installs a
 * {@code PlayerNextDamageRedirectShield}, which protects the player alone and not their
 * permanents. Both are cleared at turn cleanup.</p>
 *
 * <p>Cards: Mirrorwood Treefolk (next event), Zhalfirin Crusader, Zealous Inquisitor,
 * Personal Incarnation (protected = source),
 * Martyrdom, Hazduhr the Abbot, Daughter of Autumn, Vassal's Duty (protected = target).</p>
 *
 * @param protectedRole   whose incoming damage is redirected
 * @param destinationRole who the redirected damage is dealt to instead
 * @param amount          how much damage is redirected before the shield is consumed
 * @param declaredTarget  what the ability targets, or {@code null} when both roles are derivable
 *                        without a target (Personal Incarnation)
 * @param targetPredicate an optional narrowing predicate on a permanent target ("target white
 *                        creature"), or {@code null} when the declared target alone suffices
 * @param nextEvent       whether the next damage event is redirected in full instead of redirecting
 *                        a fixed or dynamic amount
 */
public record RedirectNextDamageEffect(RedirectRole protectedRole,
                                       RedirectRole destinationRole,
                                       DynamicAmount amount,
                                       TargetPredicate declaredTarget,
                                       PermanentPredicate targetPredicate,
                                       boolean nextEvent) implements CardEffect {

    public RedirectNextDamageEffect(RedirectRole protectedRole, RedirectRole destinationRole,
                                    DynamicAmount amount, TargetPredicate declaredTarget) {
        this(protectedRole, destinationRole, amount, declaredTarget, null, false);
    }

    public RedirectNextDamageEffect(RedirectRole protectedRole, RedirectRole destinationRole,
                                    int amount, TargetPredicate declaredTarget) {
        this(protectedRole, destinationRole, new Fixed(amount), declaredTarget, null, false);
    }

    public RedirectNextDamageEffect(RedirectRole protectedRole, RedirectRole destinationRole,
                                    DynamicAmount amount, TargetPredicate declaredTarget,
                                    PermanentPredicate targetPredicate) {
        this(protectedRole, destinationRole, amount, declaredTarget, targetPredicate, false);
    }

    public static RedirectNextDamageEffect nextEvent(RedirectRole protectedRole,
                                                      RedirectRole destinationRole,
                                                      TargetPredicate declaredTarget) {
        return new RedirectNextDamageEffect(protectedRole, destinationRole, new Fixed(1),
                declaredTarget, null, true);
    }

    /**
     * The target is harmful exactly when it is the redirect <em>destination</em>: that object is
     * the one that ends up taking the damage, so protection from the ability's source must stop it
     * being targeted (CR 702.16b). When the target is the protected object the ability only shields
     * it, so no protection check applies.
     */
    @Override
    public TargetSpec targetSpec() {
        if (declaredTarget == null) {
            return TargetSpec.NONE;
        }
        return destinationRole == RedirectRole.TARGET
                ? TargetSpec.harmful(declaredTarget, targetPredicate)
                : TargetSpec.benign(declaredTarget, targetPredicate);
    }
}
