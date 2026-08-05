package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * "You may cast a [filter] spell from among the cards in target player's hand without paying its
 * mana cost." (Mindclaw Shaman, paired with {@link RevealTargetHandEffect} for the reveal.)
 *
 * <p>Same may-cast routing as {@link MayCastAnySpellFromHandWithoutPayingManaCostEffect}, but the
 * eligible cards come from the <em>targeted</em> player's hand while the effect's controller makes
 * the choice and casts the spell. Casting one clears the remaining offers, so only a single spell
 * is cast. A {@code null} {@code spellFilter} matches every nonland card.
 *
 * @param spellFilter which of the target's hand cards are eligible ({@code null} = any nonland)
 */
public record MayCastFromTargetPlayerHandWithoutPayingManaCostEffect(CardPredicate spellFilter)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
