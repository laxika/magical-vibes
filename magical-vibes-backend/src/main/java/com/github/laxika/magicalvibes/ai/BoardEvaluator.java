package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.GameQueryService;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Read-only evaluation function that scores a game state from the AI's perspective.
 * Higher scores are better for the AI player.
 */
public class BoardEvaluator {

    private static final double LIFE_WEIGHT = 2.0;
    private static final double CARD_ADVANTAGE_WEIGHT = 6.0;
    private static final double CREATURE_WEIGHT = 1.0;
    private static final double MANA_SOURCE_WEIGHT = 1.0;
    private static final double NON_CREATURE_PERMANENT_WEIGHT = 1.0;
    private static final double LOW_LIFE_BONUS = 10.0;
    private static final double GAME_OVER_SCORE = 100000.0;

    private final GameQueryService gameQueryService;

    public BoardEvaluator(GameQueryService gameQueryService) {
        this.gameQueryService = gameQueryService;
    }

    /**
     * Evaluates the board state from the perspective of the given player.
     * Returns a score where higher is better for the AI.
     */
    public double evaluate(GameData gameData, UUID aiPlayerId) {
        UUID opponentId = getOpponentId(gameData, aiPlayerId);

        int aiLife = gameData.playerLifeTotals.getOrDefault(aiPlayerId, 20);
        int oppLife = gameData.playerLifeTotals.getOrDefault(opponentId, 20);
        int aiPoison = gameData.playerPoisonCounters.getOrDefault(aiPlayerId, 0);
        int oppPoison = gameData.playerPoisonCounters.getOrDefault(opponentId, 0);

        // Game-over detection
        if (oppLife <= 0 || oppPoison >= 10) return GAME_OVER_SCORE;
        if (aiLife <= 0 || aiPoison >= 10) return -GAME_OVER_SCORE;

        double score = 0.0;

        // Life differential
        score += (aiLife - oppLife) * LIFE_WEIGHT;

        // Low-life bonus/penalty
        if (oppLife <= 5) score += LOW_LIFE_BONUS;
        if (aiLife <= 5) score -= LOW_LIFE_BONUS;

        // Poison counter pressure
        if (oppPoison >= 7) score += LOW_LIFE_BONUS;
        if (aiPoison >= 7) score -= LOW_LIFE_BONUS;
        score += (oppPoison - aiPoison) * LIFE_WEIGHT;

        // Card advantage
        int aiHandSize = gameData.playerHands.getOrDefault(aiPlayerId, List.of()).size();
        int oppHandSize = gameData.playerHands.getOrDefault(opponentId, List.of()).size();
        score += (aiHandSize - oppHandSize) * CARD_ADVANTAGE_WEIGHT;

        // Battlefield evaluation
        List<Permanent> aiBattlefield = gameData.playerBattlefields.getOrDefault(aiPlayerId, List.of());
        List<Permanent> oppBattlefield = gameData.playerBattlefields.getOrDefault(opponentId, List.of());

        double aiCreatureQuality = 0;
        double oppCreatureQuality = 0;
        int aiLandCount = 0;
        int oppLandCount = 0;
        double aiNonCreatureValue = 0;
        double oppNonCreatureValue = 0;

        for (Permanent perm : aiBattlefield) {
            if (gameQueryService.isCreature(gameData, perm)) {
                aiCreatureQuality += creatureScore(gameData, perm, aiPlayerId, opponentId);
            }
            if (perm.getCard().getType() == CardType.LAND) {
                aiLandCount++;
            }
            if (perm.getCard().getType() == CardType.ENCHANTMENT || perm.getCard().getType() == CardType.ARTIFACT) {
                if (!gameQueryService.isCreature(gameData, perm)) {
                    aiNonCreatureValue += perm.getCard().getManaValue() * 3.0;
                }
            }
        }

        for (Permanent perm : oppBattlefield) {
            if (gameQueryService.isCreature(gameData, perm)) {
                oppCreatureQuality += creatureScore(gameData, perm, opponentId, aiPlayerId);
            }
            if (perm.getCard().getType() == CardType.LAND) {
                oppLandCount++;
            }
            if (perm.getCard().getType() == CardType.ENCHANTMENT || perm.getCard().getType() == CardType.ARTIFACT) {
                if (!gameQueryService.isCreature(gameData, perm)) {
                    oppNonCreatureValue += perm.getCard().getManaValue() * 3.0;
                }
            }
        }

        score += (aiCreatureQuality - oppCreatureQuality) * CREATURE_WEIGHT;
        score += (aiLandCount - oppLandCount) * MANA_SOURCE_WEIGHT;
        score += (aiNonCreatureValue - oppNonCreatureValue) * NON_CREATURE_PERMANENT_WEIGHT;

        return score;
    }

    /**
     * Scores a creature permanent based on its effective stats and keywords.
     */
    public double creatureScore(GameData gameData, Permanent perm, UUID controllerId, UUID opponentId) {
        int power = gameQueryService.getEffectivePower(gameData, perm);
        int toughness = gameQueryService.getEffectiveToughness(gameData, perm);

        double score = power * 3.0 + toughness * 1.5;
        score += keywordBonus(gameData, perm, opponentId);

        return score;
    }

    /**
     * Scores a creature card (not yet on the battlefield) based on its base stats and keywords.
     */
    public double creatureCardScore(Card card) {
        int power = card.getPower() != null ? card.getPower() : 0;
        int toughness = card.getToughness() != null ? card.getToughness() : 0;

        double score = power * 3.0 + toughness * 1.5;

        Set<Keyword> keywords = card.getKeywords();
        if (keywords.contains(Keyword.FLYING)) score += 4;
        if (keywords.contains(Keyword.FIRST_STRIKE)) score += 3;
        if (keywords.contains(Keyword.DOUBLE_STRIKE)) score += 6;
        if (keywords.contains(Keyword.TRAMPLE)) score += 2;
        if (keywords.contains(Keyword.VIGILANCE)) score += 2;
        if (keywords.contains(Keyword.LIFELINK)) score += 3;
        if (keywords.contains(Keyword.INDESTRUCTIBLE)) score += 5;
        if (keywords.contains(Keyword.MENACE)) score += 2;
        if (keywords.contains(Keyword.HEXPROOF)) score += 3;
        if (keywords.contains(Keyword.SHROUD)) score += 3;
        if (keywords.contains(Keyword.FEAR)) score += 2;
        if (keywords.contains(Keyword.INTIMIDATE)) score += 2;
        if (keywords.contains(Keyword.DEFENDER)) score -= 3;
        if (keywords.contains(Keyword.HASTE)) score += 1;
        if (keywords.contains(Keyword.INFECT)) score += 4;

        return score;
    }

    private double keywordBonus(GameData gameData, Permanent perm, UUID opponentId) {
        double bonus = 0;

        if (gameQueryService.hasKeyword(gameData, perm, Keyword.FLYING)) bonus += 4;
        if (gameQueryService.hasKeyword(gameData, perm, Keyword.FIRST_STRIKE)) bonus += 3;
        if (gameQueryService.hasKeyword(gameData, perm, Keyword.DOUBLE_STRIKE)) bonus += 6;
        if (gameQueryService.hasKeyword(gameData, perm, Keyword.TRAMPLE)) bonus += 2;
        if (gameQueryService.hasKeyword(gameData, perm, Keyword.VIGILANCE)) bonus += 2;
        if (gameQueryService.hasKeyword(gameData, perm, Keyword.LIFELINK)) bonus += 3;
        if (gameQueryService.hasKeyword(gameData, perm, Keyword.INDESTRUCTIBLE)) bonus += 5;
        if (gameQueryService.hasKeyword(gameData, perm, Keyword.MENACE)) bonus += 2;
        if (gameQueryService.hasKeyword(gameData, perm, Keyword.HEXPROOF)) bonus += 3;
        if (gameQueryService.hasKeyword(gameData, perm, Keyword.SHROUD)) bonus += 3;
        if (gameQueryService.hasKeyword(gameData, perm, Keyword.FEAR)) bonus += 2;
        if (gameQueryService.hasKeyword(gameData, perm, Keyword.INTIMIDATE)) bonus += 2;
        if (gameQueryService.hasKeyword(gameData, perm, Keyword.DEFENDER)) bonus -= 3;
        if (gameQueryService.hasKeyword(gameData, perm, Keyword.HASTE)) bonus += 1;
        if (gameQueryService.hasKeyword(gameData, perm, Keyword.INFECT)) bonus += 4;

        // Landwalk bonuses: check if opponent has matching land type
        if (opponentId != null) {
            List<Permanent> oppBattlefield = gameData.playerBattlefields.getOrDefault(opponentId, List.of());
            if (gameQueryService.hasKeyword(gameData, perm, Keyword.FORESTWALK)
                    && oppBattlefield.stream().anyMatch(p -> p.getCard().getSubtypes().contains(CardSubtype.FOREST))) {
                bonus += 2;
            }
            if (gameQueryService.hasKeyword(gameData, perm, Keyword.MOUNTAINWALK)
                    && oppBattlefield.stream().anyMatch(p -> p.getCard().getSubtypes().contains(CardSubtype.MOUNTAIN))) {
                bonus += 2;
            }
            if (gameQueryService.hasKeyword(gameData, perm, Keyword.ISLANDWALK)
                    && oppBattlefield.stream().anyMatch(p -> p.getCard().getSubtypes().contains(CardSubtype.ISLAND))) {
                bonus += 2;
            }
            if (gameQueryService.hasKeyword(gameData, perm, Keyword.SWAMPWALK)
                    && oppBattlefield.stream().anyMatch(p -> p.getCard().getSubtypes().contains(CardSubtype.SWAMP))) {
                bonus += 2;
            }
        }

        return bonus;
    }

    private UUID getOpponentId(GameData gameData, UUID playerId) {
        for (UUID id : gameData.orderedPlayerIds) {
            if (!id.equals(playerId)) {
                return id;
            }
        }
        return null;
    }
}
