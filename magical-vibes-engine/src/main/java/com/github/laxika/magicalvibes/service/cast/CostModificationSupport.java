package com.github.laxika.magicalvibes.service.cast;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

/**
 * Shared game-state queries used by {@link CostModificationHandlerBean} implementations
 * and {@code CastingCostService}.
 */
@Component
@RequiredArgsConstructor
public class CostModificationSupport {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;

    public boolean sharesCardType(Card spell, Card imprinted) {
        EnumSet<CardType> spellTypes = EnumSet.of(spell.getType());
        spellTypes.addAll(spell.getAdditionalTypes());

        EnumSet<CardType> imprintedTypes = EnumSet.of(imprinted.getType());
        imprintedTypes.addAll(imprinted.getAdditionalTypes());

        for (CardType type : spellTypes) {
            if (imprintedTypes.contains(type)) {
                return true;
            }
        }
        return false;
    }

    public boolean anyOpponentControlsAtLeastNMoreCreatures(GameData gameData, UUID playerId, int minimumDifference) {
        int yourCreatures = countCreaturesControlled(gameData, playerId);
        for (UUID candidateOpponentId : gameData.orderedPlayerIds) {
            if (candidateOpponentId.equals(playerId)) {
                continue;
            }
            int opponentCreatures = countCreaturesControlled(gameData, candidateOpponentId);
            if (opponentCreatures >= yourCreatures + minimumDifference) {
                return true;
            }
        }
        return false;
    }

    public int countCreaturesControlled(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return 0;
        }
        int count = 0;
        for (Permanent permanent : battlefield) {
            if (gameQueryService.isCreature(gameData, permanent)) {
                count++;
            }
        }
        return count;
    }

    public int countCreaturesOnAllBattlefields(GameData gameData) {
        int total = 0;
        for (UUID pid : gameData.orderedPlayerIds) {
            total += countCreaturesControlled(gameData, pid);
        }
        return total;
    }

    public int countCreatureCardsInGraveyard(GameData gameData, UUID playerId) {
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        if (graveyard == null) return 0;
        int count = 0;
        for (Card card : graveyard) {
            if (card.hasType(CardType.CREATURE)) {
                count++;
            }
        }
        return count;
    }

    public boolean controlsPermanent(GameData gameData, UUID playerId, PermanentPredicate predicate) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return false;
        for (Permanent p : battlefield) {
            if (predicateEvaluationService.matchesPermanentPredicate(gameData, p, predicate)) {
                return true;
            }
        }
        return false;
    }

    public boolean battlefieldHasPermanentMatching(GameData gameData, PermanentPredicate predicate) {
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            if (battlefield == null) {
                continue;
            }
            for (Permanent p : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(gameData, p, predicate)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean stackHasMatchingSpell(GameData gameData, StackEntryPredicate predicate) {
        for (StackEntry entry : gameData.stack) {
            if (predicateEvaluationService.matchesStackEntryPredicate(entry, predicate, null)) {
                return true;
            }
        }
        return false;
    }
}
