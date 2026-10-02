package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.Objects;

/**
 * Returns a target creature card from the controller's graveyard to hand after perpetually
 * modifying its subtype, base power and toughness, and alternate hand-casting cost.
 */
public record ReturnTargetCreatureCardToHandAndPerpetuallyModifyEffect(
        CardSubtype subtype,
        int power,
        int toughness,
        String alternateManaCost
) implements CardEffect {

    public ReturnTargetCreatureCardToHandAndPerpetuallyModifyEffect {
        Objects.requireNonNull(subtype, "subtype");
        Objects.requireNonNull(alternateManaCost, "alternateManaCost");
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(
                new CardTypePredicate(CardType.CREATURE),
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
    }
}
