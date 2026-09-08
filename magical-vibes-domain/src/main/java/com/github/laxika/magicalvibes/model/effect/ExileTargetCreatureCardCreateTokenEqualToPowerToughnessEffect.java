package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

/**
 * Exiles the targeted creature card from the controller's graveyard, then creates one token
 * whose power and toughness are copied from the card's printed power and toughness.
 *
 * @param tokenTemplate token characteristics to use, with power and toughness replaced at
 *                      resolution
 */
public record ExileTargetCreatureCardCreateTokenEqualToPowerToughnessEffect(
        CreateTokenEffect tokenTemplate
) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(
                new CardTypePredicate(CardType.CREATURE), GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
    }
}
