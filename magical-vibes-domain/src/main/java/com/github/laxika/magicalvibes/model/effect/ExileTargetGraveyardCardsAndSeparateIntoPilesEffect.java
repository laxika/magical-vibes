package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardPileDisposition;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Exile up to {@code maxTargets} target cards matching the filter from the declared graveyard
 * scope, then separate them into two piles. The disposition and separating/choosing roles are
 * configurable for Boneyard Parley and Split the Spoils.
 *
 * <p>Flow:
 * <ol>
 *   <li>At cast time: controller targets up to {@code maxTargets} cards matching
 *       {@code filter} from {@code graveyardScope}.</li>
 *   <li>On resolution: targeted cards are exiled from their graveyards.</li>
 *   <li>The configured separator assigns the exiled cards to two piles.</li>
 *   <li>The configured chooser selects a pile (Yes = Pile 1, No = Pile 2).</li>
 *   <li>The configured {@code disposition} moves the chosen and other piles.</li>
 * </ol>
 *
 * <p>Multi-target graveyard selection is handled by SpellCastingService at cast time.
 * Targets are stored in StackEntry.targetCardIds and resolved by GraveyardReturnResolutionService.
 */
public record ExileTargetGraveyardCardsAndSeparateIntoPilesEffect(
        CardPredicate filter,
        int maxTargets,
        GraveyardSearchScope graveyardScope,
        CardPileDisposition disposition,
        boolean controllerSeparates
) implements CardEffect {

    /** Boneyard Parley: target matching cards from any graveyard. */
    public ExileTargetGraveyardCardsAndSeparateIntoPilesEffect(CardPredicate filter, int maxTargets) {
        this(filter, maxTargets, GraveyardSearchScope.ALL_GRAVEYARDS,
                CardPileDisposition.BATTLEFIELD, false);
    }

    public ExileTargetGraveyardCardsAndSeparateIntoPilesEffect(CardPredicate filter, int maxTargets,
                                                                GraveyardSearchScope graveyardScope) {
        this(filter, maxTargets, graveyardScope, CardPileDisposition.BATTLEFIELD, false);
    }

    @Override
    public TargetSpec targetSpec() {
        if (graveyardScope == GraveyardSearchScope.ALL_GRAVEYARDS
                && disposition == CardPileDisposition.BATTLEFIELD
                && !controllerSeparates) {
            return TargetSpec.NONE;
        }
        return TargetSpec.benign(TargetPredicates.graveyardCards(filter, graveyardScope));
    }
}
