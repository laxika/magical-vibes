package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * "You may cast a [filter] spell from your hand without paying its mana cost."
 * (Maelstrom Archangel — any spell; Wildfire Eternal — instant or sorcery)
 *
 * <p>On resolution the controller is offered each matching nonland hand card as a
 * may-cast-from-hand-without-paying choice via {@link MayCastFromHandWithoutPayingManaCostEffect};
 * casting one clears the rest, so only a single spell is cast. A {@code null}
 * {@code spellFilter} matches every nonland hand card.
 *
 * @param spellFilter which hand cards are eligible ({@code null} = any nonland)
 */
public record MayCastAnySpellFromHandWithoutPayingManaCostEffect(CardPredicate spellFilter)
        implements CardEffect {

    /** Any nonland spell (Maelstrom Archangel). */
    public MayCastAnySpellFromHandWithoutPayingManaCostEffect() {
        this(null);
    }
}
