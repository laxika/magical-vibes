package com.github.laxika.magicalvibes.model.effect;

/**
 * "The next time this creature would deal combat damage to an opponent this turn, it deals that
 * damage to target creature instead." (Soltari Guerrillas.)
 *
 * <p>A redirection (replacement) effect keyed on the ability's source permanent: the protected
 * party is whichever opponent would have taken the combat damage, and the destination is the
 * creature targeted on activation. Only the next such damage event is redirected, and only combat
 * damage dealt to a player — the source's damage to a planeswalker or to a blocking creature is
 * untouched. The shield lives in {@code GameData.sourceNextCombatDamageToOpponentRedirectShields}
 * and is cleared at turn cleanup.</p>
 *
 * <p>Unlike {@link RedirectNextDamageEffect}, which shields an object from incoming damage, this
 * one diverts the outgoing damage of a single source, so it cannot be expressed as a
 * {@code RedirectRole} pair — the protected end is not known until combat damage is assigned.</p>
 */
public record RedirectNextCombatDamageToOpponentEffect() implements CardEffect {

    /**
     * Harmful: the targeted creature is the one that ends up taking the damage, so protection from
     * the source must stop it being targeted (CR 702.16b).
     */
    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
