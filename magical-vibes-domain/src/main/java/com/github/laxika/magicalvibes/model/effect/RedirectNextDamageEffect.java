package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "The next {@code amount} damage that would be dealt to X this turn is dealt to Y instead."
 * Both ends of the redirection are roles rather than fixed objects, which is the only axis the
 * whole family varies on: {@code protectedRole} is whose incoming damage is diverted and
 * {@code destinationRole} is who receives it instead. The shield matches damage from any source
 * (combat or noncombat) and is consumed once {@code amount} damage has been redirected.
 *
 * <p>A permanent-protecting instance installs an amount-limited
 * {@code CreatureDamageRedirectShield}; a player-protecting one (reachable only when
 * {@code protectedRole} resolves to a player, as with Martyrdom's any-target grant) installs a
 * {@code PlayerNextDamageRedirectShield}, which protects the player alone and not their
 * permanents. Both are cleared at turn cleanup.</p>
 *
 * <p>Cards: Zhalfirin Crusader, Zealous Inquisitor, Personal Incarnation (protected = source),
 * Martyrdom, Hazduhr the Abbot, Daughter of Autumn, Vassal's Duty (protected = target).</p>
 *
 * @param protectedRole   whose incoming damage is redirected
 * @param destinationRole who the redirected damage is dealt to instead
 * @param amount          how much damage is redirected before the shield is consumed
 * @param targetCategory  what the ability targets, or {@link TargetCategory#NONE} when both roles
 *                        are derivable without a target (Personal Incarnation)
 * @param targetPredicate an optional narrowing predicate on a permanent target ("target white
 *                        creature"), or {@code null} when the category alone suffices
 */
public record RedirectNextDamageEffect(RedirectRole protectedRole,
                                       RedirectRole destinationRole,
                                       DynamicAmount amount,
                                       TargetCategory targetCategory,
                                       PermanentPredicate targetPredicate) implements CardEffect {

    public RedirectNextDamageEffect(RedirectRole protectedRole, RedirectRole destinationRole,
                                    DynamicAmount amount, TargetCategory targetCategory) {
        this(protectedRole, destinationRole, amount, targetCategory, null);
    }

    public RedirectNextDamageEffect(RedirectRole protectedRole, RedirectRole destinationRole,
                                    int amount, TargetCategory targetCategory) {
        this(protectedRole, destinationRole, new Fixed(amount), targetCategory, null);
    }

    /**
     * The target is harmful exactly when it is the redirect <em>destination</em>: that object is
     * the one that ends up taking the damage, so protection from the ability's source must stop it
     * being targeted (CR 702.16b). When the target is the protected object the ability only shields
     * it, so no protection check applies.
     */
    @Override
    public TargetSpec targetSpec() {
        if (targetCategory == TargetCategory.NONE) {
            return TargetSpec.NONE;
        }
        return destinationRole == RedirectRole.TARGET
                ? TargetSpec.harmful(targetCategory, targetPredicate)
                : TargetSpec.benign(targetCategory, targetPredicate);
    }
}
