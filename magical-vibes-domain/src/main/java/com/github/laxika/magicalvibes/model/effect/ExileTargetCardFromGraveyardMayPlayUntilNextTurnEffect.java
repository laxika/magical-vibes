package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
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
 * @param whileSourceControlled when {@code true}, the permission lasts while the source permanent
 *                             remains under the controller's control instead of expiring next turn
 */
public record ExileTargetCardFromGraveyardMayPlayUntilNextTurnEffect(
        CardPredicate filter,
        boolean ownGraveyardOnly,
        boolean whileSourceControlled
) implements CardEffect {

    public ExileTargetCardFromGraveyardMayPlayUntilNextTurnEffect(
            CardPredicate filter, boolean ownGraveyardOnly) {
        this(filter, ownGraveyardOnly, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCard(ownGraveyardOnly
                ? GraveyardSearchScope.CONTROLLERS_GRAVEYARD
                : GraveyardSearchScope.ALL_GRAVEYARDS));
    }
}
