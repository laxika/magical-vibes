package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;

import java.util.List;
import java.util.UUID;

/**
 * Evaluates the "race" — the comparison of how quickly each player can kill the other
 * with their current board and burn spells in hand. Used by the Hard AI to decide
 * whether to play aggressively or defensively.
 */
public class RaceEvaluator {

    private final GameQueryService gameQueryService;

    /**
     * Snapshot of the race state between two players.
     *
     * @param aiClock          turns until AI kills opponent (Integer.MAX_VALUE if no damage)
     * @param opponentClock    turns until opponent kills AI (Integer.MAX_VALUE if no damage)
     * @param aiBoardDamage    total damage AI's creatures can deal per turn if all attack unblocked
     * @param opponentBoardDamage total damage opponent's creatures can deal per turn
     * @param burnInHandDamage total burn-to-face damage in AI's hand (castable right now)
     * @param burnLethal       true if burn spells in hand can kill opponent right now
     */
    public record RaceState(int aiClock, int opponentClock,
                            int aiBoardDamage, int opponentBoardDamage,
                            int burnInHandDamage, boolean burnLethal) {

        /**
         * Returns true if the AI is winning the race (kills opponent before opponent kills AI).
         * A tie (same clock) is not considered "winning" — both would die the same turn.
         */
        public boolean aiWinningRace() {
            return aiClock < opponentClock;
        }

        /**
         * Returns true if the AI is losing the race (opponent kills AI first).
         */
        public boolean aiLosingRace() {
            return opponentClock < aiClock;
        }

        /**
         * Returns true if board damage alone would kill opponent next attack (1-turn clock).
         */
        public boolean boardLethalNextAttack() {
            return aiClock <= 1;
        }
    }

    public RaceEvaluator(GameQueryService gameQueryService) {
        this.gameQueryService = gameQueryService;
    }

    /**
     * Evaluates the race between AI and opponent.
     *
     * @param gameData    current game state
     * @param aiPlayerId  the AI's player id
     * @param castableBurnCards burn spells the AI can currently cast (already filtered for affordability)
     */
    public RaceState evaluate(GameData gameData, UUID aiPlayerId, List<Card> castableBurnCards) {
        UUID opponentId = AiUtils.getOpponentId(gameData, aiPlayerId);

        int opponentLife = gameData.getLife(opponentId);
        int aiLife = gameData.getLife(aiPlayerId);

        int aiBoardDamage = calculateBoardDamage(gameData, aiPlayerId);
        int opponentBoardDamage = calculateBoardDamage(gameData, opponentId);

        int aiClock = calculateClock(aiBoardDamage, opponentLife);
        int opponentClock = calculateClock(opponentBoardDamage, aiLife);

        int burnDamage = calculateBurnInHandDamage(castableBurnCards);
        boolean burnLethal = burnDamage >= opponentLife;

        return new RaceState(aiClock, opponentClock, aiBoardDamage, opponentBoardDamage,
                burnDamage, burnLethal);
    }

    /**
     * Calculates the total damage a player's creatures can deal per turn if they
     * all attack unblocked. Only counts non-defender creatures that are untapped
     * or have vigilance, and that aren't summoning sick (unless they have haste).
     */
    int calculateBoardDamage(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.getOrDefault(playerId, List.of());
        int totalDamage = 0;

        for (Permanent perm : battlefield) {
            if (!gameQueryService.isCreature(gameData, perm)) continue;
            if (gameQueryService.hasKeyword(gameData, perm, Keyword.DEFENDER)) continue;

            // Skip summoning sick creatures without haste
            if (perm.isSummoningSick()
                    && !gameQueryService.hasKeyword(gameData, perm, Keyword.HASTE)) {
                continue;
            }

            // Tapped creatures can't attack (unless vigilance keeps them untapped after attacking,
            // but if they're already tapped they can't attack this turn)
            if (perm.isTapped()) continue;

            int power = gameQueryService.getEffectivePower(gameData, perm);
            if (power > 0) {
                totalDamage += power;
            }
        }

        return totalDamage;
    }

    /**
     * Calculates the number of turns to kill given damage per turn and target life total.
     * Returns Integer.MAX_VALUE if damage is 0 (infinite clock).
     */
    static int calculateClock(int damagePerTurn, int life) {
        if (damagePerTurn <= 0) return Integer.MAX_VALUE;
        return (life + damagePerTurn - 1) / damagePerTurn; // ceiling division
    }

    /**
     * Calculates total direct-to-face damage available from burn spells in hand.
     * Counts spells that can deal damage to a player (DealDamageToAnyTargetEffect,
     * DealDamageToTargetPlayerEffect, etc.). Only includes spells passed in as castable.
     */
    int calculateBurnInHandDamage(List<Card> castableBurnCards) {
        int totalDamage = 0;

        for (Card card : castableBurnCards) {
            totalDamage += getBurnToFaceDamage(card);
        }

        return totalDamage;
    }

    /**
     * Returns the face damage a single card can deal to a player, or 0 if none.
     * Checks SPELL effects for damage-to-player or damage-to-any-target effects.
     * For modal spells, returns the maximum possible face damage mode.
     */
    int getBurnToFaceDamage(Card card) {
        int maxDamage = 0;

        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            if (effect instanceof ChooseOneEffect coe) {
                for (ChooseOneEffect.ChooseOneOption option : coe.options()) {
                    maxDamage = Math.max(maxDamage, getSingleEffectFaceDamage(option.effect()));
                }
                continue;
            }
            maxDamage = Math.max(maxDamage, getSingleEffectFaceDamage(effect));
        }

        return maxDamage;
    }

    private int getSingleEffectFaceDamage(CardEffect effect) {
        if (effect instanceof DealDamageToAnyTargetEffect dmg) {
            return dmg.damage();
        }
        if (effect instanceof DealDamageToTargetPlayerEffect dmg) {
            return dmg.damage();
        }
        // X spells are excluded — their damage depends on available mana which
        // varies and is harder to calculate accurately. The main purpose of this
        // check is for fixed-damage burn like Lightning Bolt and Shock.
        return 0;
    }
}
