package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.condition.Condition;

/**
 * "Target player reveals the top {@code count} cards of their library. You may cast an instant or
 * sorcery spell from among them without paying its mana cost. Then that player puts the rest into
 * their graveyard." (Talent of the Telepath.)
 *
 * <p>{@code extraCastCondition} is the card's spell-mastery style gate: when it is met the
 * controller may cast up to two spells instead of one. Pass {@code null} for a plain single cast.
 *
 * <p>The revealed cards are held outside every zone while the casting decisions are made, so a card
 * that is cast never touches the graveyard; only the cards left over when the offers are exhausted
 * are put into the revealing player's graveyard.
 */
public record RevealTopCardsOfTargetPlayerAndCastInstantOrSorceryEffect(int count,
                                                                       Condition extraCastCondition)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
