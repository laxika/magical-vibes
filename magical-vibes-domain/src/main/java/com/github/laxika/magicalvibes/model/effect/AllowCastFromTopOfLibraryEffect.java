package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CastingCost;
import com.github.laxika.magicalvibes.model.RemoveCountersFromControlledCreaturesCastingCost;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;
import java.util.OptionalInt;
import java.util.Set;

/**
 * Static marker effect: "You may cast [types] from the top of your library."
 * While a permanent with this effect is on the battlefield, the controller may
 * cast spells of the specified types or matching the optional card predicate from the top of their
 * library (paying their mana cost normally). The optional colorless clause is separate from the
 * type clause because colorless is a characteristic, not a card type.
 */
public record AllowCastFromTopOfLibraryEffect(Set<CardType> castableTypes, boolean castableColorless,
                                              CardPredicate filter, boolean oncePerTurn,
                                              List<CastingCost> additionalCosts)
        implements CardEffect {

    public AllowCastFromTopOfLibraryEffect {
        additionalCosts = additionalCosts == null ? List.of() : List.copyOf(additionalCosts);
    }

    public AllowCastFromTopOfLibraryEffect(Set<CardType> castableTypes, boolean castableColorless,
                                           CardPredicate filter, boolean oncePerTurn) {
        this(castableTypes, castableColorless, filter, oncePerTurn, List.of());
    }

    public AllowCastFromTopOfLibraryEffect(Set<CardType> castableTypes) {
        this(castableTypes, false, null, false, List.of());
    }

    public AllowCastFromTopOfLibraryEffect(Set<CardType> castableTypes, boolean castableColorless) {
        this(castableTypes, castableColorless, null, false, List.of());
    }

    public AllowCastFromTopOfLibraryEffect(Set<CardType> castableTypes,
                                           List<? extends CastingCost> additionalCosts) {
        this(castableTypes, false, null, false, List.copyOf(additionalCosts));
    }

    public AllowCastFromTopOfLibraryEffect(CardPredicate filter) {
        this(filter, false);
    }

    public AllowCastFromTopOfLibraryEffect(CardPredicate filter, boolean oncePerTurn) {
        this(Set.of(), false, filter, oncePerTurn);
    }

    public boolean matches(Card card) {
        if (card.getType() == CardType.LAND) return false;
        boolean matchesType = castableTypes.contains(card.getType())
                || card.getAdditionalTypes().stream().anyMatch(castableTypes::contains);
        boolean matchesColorless = castableColorless
                && (card.getColors() == null || card.getColors().isEmpty());
        return matchesType || matchesColorless;
    }

    public OptionalInt counterRemovalCost() {
        if (additionalCosts.size() != 1
                || !(additionalCosts.getFirst() instanceof RemoveCountersFromControlledCreaturesCastingCost cost)) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(cost.count());
    }
}
