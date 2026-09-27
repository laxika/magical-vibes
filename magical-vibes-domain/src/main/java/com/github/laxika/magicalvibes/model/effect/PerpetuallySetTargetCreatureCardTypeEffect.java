package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.Objects;

/**
 * Perpetually changes a target creature card in the controller's graveyard to exactly one card
 * type, removing all of its other card types. The handler replaces the frozen card with a same-id
 * runtime copy so the changed characteristics travel with that physical card through later zones.
 */
public record PerpetuallySetTargetCreatureCardTypeEffect(CardType cardType) implements CardEffect {

    public PerpetuallySetTargetCreatureCardTypeEffect {
        Objects.requireNonNull(cardType, "cardType");
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(
                new CardTypePredicate(CardType.CREATURE),
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
    }
}
