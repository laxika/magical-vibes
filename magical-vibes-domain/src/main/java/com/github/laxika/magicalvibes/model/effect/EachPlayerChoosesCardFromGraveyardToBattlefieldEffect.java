package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Set;

/**
 * The controller chooses one matching card from each player's graveyard and puts those cards
 * onto the battlefield under the controller's control.
 *
 * @param filter predicate restricting the cards that may be chosen
 */
public record EachPlayerChoosesCardFromGraveyardToBattlefieldEffect(
        CardPredicate filter,
        CardColor grantColor,
        CardSubtype grantSubtype,
        Set<Keyword> grantKeywords)
        implements CardEffect {

    public EachPlayerChoosesCardFromGraveyardToBattlefieldEffect(CardPredicate filter) {
        this(filter, null, null, Set.of());
    }

    public EachPlayerChoosesCardFromGraveyardToBattlefieldEffect {
        grantKeywords = grantKeywords == null ? Set.of() : Set.copyOf(grantKeywords);
    }
}
