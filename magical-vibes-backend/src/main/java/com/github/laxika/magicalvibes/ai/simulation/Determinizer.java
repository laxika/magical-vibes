package com.github.laxika.magicalvibes.ai.simulation;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Randomizes hidden information for Information Set MCTS (IS-MCTS).
 * Creates a "determinization" by shuffling unknown cards (opponent's hand, both decks)
 * while preserving all publicly known information.
 */
public class Determinizer {

    /**
     * Produces a determinized copy of the game state where hidden information
     * (opponent's hand, opponent's deck, AI's deck order) is randomized.
     *
     * @param gd         The game state to determinize (will be deep-copied first)
     * @param aiPlayerId The AI player's ID (whose hand is known and preserved)
     * @param rng        Random source for shuffling
     * @return A new GameData with randomized hidden info
     */
    public GameData determinize(GameData gd, UUID aiPlayerId, Random rng) {
        GameData copy = gd.deepCopy();

        UUID opponentId = null;
        for (UUID id : copy.orderedPlayerIds) {
            if (!id.equals(aiPlayerId)) {
                opponentId = id;
                break;
            }
        }
        if (opponentId == null) return copy;

        // Collect all cards the AI cannot see
        List<Card> unknownPool = new ArrayList<>();

        // Opponent's hand — unknown to AI
        List<Card> oppHand = copy.playerHands.get(opponentId);
        int oppHandSize = oppHand != null ? oppHand.size() : 0;
        if (oppHand != null) {
            unknownPool.addAll(oppHand);
            oppHand.clear();
        }

        // Opponent's deck — unknown order and content
        List<Card> oppDeck = copy.playerDecks.get(opponentId);
        if (oppDeck != null) {
            unknownPool.addAll(oppDeck);
            oppDeck.clear();
        }

        // AI's own deck — known content but unknown order
        List<Card> aiDeck = copy.playerDecks.get(aiPlayerId);
        if (aiDeck != null) {
            Collections.shuffle(aiDeck, rng);
        }

        // Shuffle the unknown pool
        Collections.shuffle(unknownPool, rng);

        // Redistribute: fill opponent's hand with the correct number of cards
        if (oppHand != null) {
            for (int i = 0; i < oppHandSize && !unknownPool.isEmpty(); i++) {
                oppHand.add(unknownPool.removeFirst());
            }
        }

        // Remaining cards go to opponent's deck
        if (oppDeck != null) {
            oppDeck.addAll(unknownPool);
        }

        return copy;
    }
}
