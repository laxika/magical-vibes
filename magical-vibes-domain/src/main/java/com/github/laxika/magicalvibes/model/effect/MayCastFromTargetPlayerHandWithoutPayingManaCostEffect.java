package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * "You may cast a [filter] spell from among the cards in target player's hand without paying its
 * mana cost." (Mindclaw Shaman, paired with {@link RevealTargetHandEffect} for the reveal.)
 * The {@link #damagedPlayer()} form uses the player dealt combat damage as non-targeting context.
 *
 * <p>Same may-cast routing as {@link MayCastAnySpellFromHandWithoutPayingManaCostEffect}, but the
 * eligible cards come from the <em>targeted</em> player's hand while the effect's controller makes
 * the choice and casts the spell. Casting one clears the remaining offers, so only a single spell
 * is cast. A {@code null} {@code spellFilter} matches every nonland card.
 *
 * @param spellFilter which of the target's hand cards are eligible ({@code null} = any nonland)
 * @param targetsPlayer whether the player is chosen as a target rather than supplied by combat
 *                      damage context
 */
public record MayCastFromTargetPlayerHandWithoutPayingManaCostEffect(CardPredicate spellFilter,
                                                                    boolean targetsPlayer)
        implements CombatDamageTriggerContextEffect {

    public MayCastFromTargetPlayerHandWithoutPayingManaCostEffect(CardPredicate spellFilter) {
        this(spellFilter, true);
    }

    /** Any nonland spell from the player dealt combat damage by the source. */
    public static MayCastFromTargetPlayerHandWithoutPayingManaCostEffect damagedPlayer() {
        return new MayCastFromTargetPlayerHandWithoutPayingManaCostEffect(null, false);
    }

    /** A filtered spell from the player dealt combat damage by the source. */
    public static MayCastFromTargetPlayerHandWithoutPayingManaCostEffect damagedPlayer(
            CardPredicate spellFilter) {
        return new MayCastFromTargetPlayerHandWithoutPayingManaCostEffect(spellFilter, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetsPlayer ? TargetSpec.harmful(TargetPredicates.player()) : TargetSpec.NONE;
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return targetsPlayer ? null : TriggerContext.DAMAGED_PLAYER;
    }
}
