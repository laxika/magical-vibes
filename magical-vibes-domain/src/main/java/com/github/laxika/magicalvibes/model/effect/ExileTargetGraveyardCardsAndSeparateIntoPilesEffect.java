package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Exile up to {@code maxTargets} target cards matching the filter from any graveyard.
 * An opponent separates those cards into two piles. Controller chooses one pile
 * to put onto the battlefield under their control; the rest go to their owners' graveyards.
 * (e.g. Boneyard Parley)
 *
 * <p>Flow:
 * <ol>
 *   <li>At cast time: controller targets up to {@code maxTargets} cards matching
 *       {@code filter} from any graveyard (all-graveyards multi-target selection).</li>
 *   <li>On resolution: targeted cards are exiled from their graveyards.</li>
 *   <li>An opponent separates the exiled cards into two piles (multi-graveyard choice).</li>
 *   <li>Controller chooses a pile (may-ability choice: Yes = Pile 1, No = Pile 2).</li>
 *   <li>Chosen pile enters the battlefield under controller's control;
 *       other pile returns to owners' graveyards.</li>
 * </ol>
 *
 * <p>Multi-target graveyard selection is handled by SpellCastingService at cast time.
 * Targets are stored in StackEntry.targetCardIds and resolved by GraveyardReturnResolutionService.
 */
public record ExileTargetGraveyardCardsAndSeparateIntoPilesEffect(
        CardPredicate filter,
        int maxTargets
) implements CardEffect {
}
