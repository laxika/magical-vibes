package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByFewerThanNCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueAtMostXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueEqualsXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;

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

    /**
     * Whether the card's target filter contains a {@link PermanentManaValueAtMostXPredicate}, so
     * the announced X must be at least the chosen target's mana value rather than being free to
     * pick.
     */
    public static boolean hasManaValueAtMostXTarget(Card card) {
        TargetFilter filter = card.getTargetFilter();
        return filter instanceof PermanentPredicateTargetFilter pf
                && containsManaValueAtMostXPredicate(pf.predicate());
    }

    private static boolean containsManaValueAtMostXPredicate(PermanentPredicate predicate) {
        if (predicate instanceof PermanentManaValueAtMostXPredicate) {
            return true;
        }
        return predicate instanceof PermanentAllOfPredicate allOf
                && allOf.predicates().stream().anyMatch(AiUtils::containsManaValueAtMostXPredicate);
    }

    /**
     * Minimum number of creatures that must be assigned together for the attacker to become
     * blocked. Multiple restrictions combine by taking the largest minimum.
     */
    static int minimumBlockersRequiredToBlock(GameData gameData, GameQueryService gameQueryService,
                                              Permanent attacker) {
        int minimum = gameQueryService.hasKeyword(gameData, attacker, Keyword.MENACE) ? 2 : 1;
        for (var effect : attacker.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof CantBeBlockedByFewerThanNCreaturesEffect restriction) {
                minimum = Math.max(minimum, restriction.minBlockers());
            }
        }
        return minimum;
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
