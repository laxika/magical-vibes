package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueEqualsXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.UUID;

/**
 * Shared static utility methods for AI decision classes.
 */
public final class AiUtils {

    private AiUtils() {}

    /**
     * Whether the card's target filter contains a {@link PermanentManaValueEqualsXPredicate}, so
     * the announced X must equal the chosen target's mana value rather than being free to pick
     * (Entrancing Melody, Detonate). Every AI X chooser has to special-case these, including the
     * MCTS simulator's action enumeration, which is why this lives here rather than on one engine.
     */
    public static boolean hasManaValueEqualsXTarget(Card card) {
        TargetFilter filter = card.getTargetFilter();
        return filter instanceof PermanentPredicateTargetFilter pf
                && containsManaValueEqualsXPredicate(pf.predicate());
    }

    private static boolean containsManaValueEqualsXPredicate(PermanentPredicate predicate) {
        if (predicate instanceof PermanentManaValueEqualsXPredicate) {
            return true;
        }
        return predicate instanceof PermanentAllOfPredicate allOf
                && allOf.predicates().stream().anyMatch(AiUtils::containsManaValueEqualsXPredicate);
    }

    static UUID getOpponentId(GameData gameData, UUID playerId) {
        for (UUID id : gameData.orderedPlayerIds) {
            if (!id.equals(playerId)) {
                return id;
            }
        }
        return null;
    }

    /**
     * Whether an AI seated as {@code aiPlayerId} must answer a choice addressed to
     * {@code choicePlayerId}. Normally only its own choices — but during a
     * Mindslaver-controlled turn the engine routes the controlled player's prompts
     * to the controlling player's connection and substitutes the acting player when
     * the answer arrives ({@code GameService.resolveActingPlayer}), so the
     * controller must answer those too.
     */
    static boolean isRespondingFor(GameData gameData, UUID aiPlayerId, UUID choicePlayerId) {
        if (aiPlayerId.equals(choicePlayerId)) {
            return true;
        }
        return aiPlayerId.equals(gameData.mindControllerPlayerId)
                && choicePlayerId != null
                && choicePlayerId.equals(gameData.mindControlledPlayerId);
    }
}
