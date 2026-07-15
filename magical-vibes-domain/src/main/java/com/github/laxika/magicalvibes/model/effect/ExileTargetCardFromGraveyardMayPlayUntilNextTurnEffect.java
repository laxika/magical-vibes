package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Exiles the targeted card from a graveyard, then grants its controller permission to play
 * (cast) that card until the end of their next turn.
 * <p>
 * Used by cards like Practiced Scrollsmith: "exile target noncreature, nonland card from your
 * graveyard. Until the end of your next turn, you may cast that card."
 *
 * @param filter           predicate restricting valid graveyard targets; {@code null} means any card
 * @param ownGraveyardOnly when {@code true}, only the controller's graveyard can be targeted
 */
public record ExileTargetCardFromGraveyardMayPlayUntilNextTurnEffect(
        CardPredicate filter,
        boolean ownGraveyardOnly
) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        // ownGraveyardOnly is enforced by the kept validator's controller-compare, not by
        // targetsControllersGraveyardOnly (which stayed false); so ownGraveyardOnly=true reproduces
        // (graveyard=T, any=F) = GRAVEYARD_CARD, not CONTROLLERS_GRAVEYARD_CARD.
        return ownGraveyardOnly
                ? TargetSpec.benign(TargetCategory.GRAVEYARD_CARD)
                : TargetSpec.benign(TargetCategory.ANY_GRAVEYARD_CARD);
    }
}
