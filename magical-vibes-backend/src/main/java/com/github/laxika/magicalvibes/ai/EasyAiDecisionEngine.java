package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.MessageHandler;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.networking.message.DeclareAttackersRequest;
import com.github.laxika.magicalvibes.networking.message.DeclareBlockersRequest;
import com.github.laxika.magicalvibes.networking.message.PassPriorityRequest;
import com.github.laxika.magicalvibes.networking.message.PlayCardRequest;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.GameRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Easy difficulty AI. Uses simple heuristics for spell casting, combat, and scoring.
 */
@Slf4j
public class EasyAiDecisionEngine extends AiDecisionEngine {

    public EasyAiDecisionEngine(UUID gameId, Player aiPlayer, GameRegistry gameRegistry,
                                MessageHandler messageHandler, GameQueryService gameQueryService) {
        super(gameId, aiPlayer, gameRegistry, messageHandler, gameQueryService);
    }

    // ===== Priority / Main Phase =====

    @Override
    protected void handleGameState(GameData gameData) {
        if (!hasPriority(gameData)) {
            return;
        }

        boolean isMainPhase = gameData.currentStep == TurnStep.PRECOMBAT_MAIN
                || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN;
        boolean isActivePlayer = aiPlayer.getId().equals(gameData.activePlayerId);

        if (isMainPhase && isActivePlayer && gameData.stack.isEmpty()) {
            if (tryPlayLand(gameData)) {
                return;
            }

            if (tryCastSpell(gameData)) {
                return;
            }
        }

        // Pass priority
        send(() -> messageHandler.handlePassPriority(selfConnection, new PassPriorityRequest()));
    }

    protected boolean tryCastSpell(GameData gameData) {
        List<Card> hand = gameData.playerHands.get(aiPlayer.getId());
        if (hand == null) {
            return false;
        }

        ManaPool virtualPool = manaManager.buildVirtualManaPool(gameData, aiPlayer.getId());

        // Score and sort castable spells
        List<int[]> castableSpells = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (card.getType() == CardType.LAND) {
                continue;
            }
            if (card.getType() == CardType.INSTANT) {
                continue;
            }
            if (card.getManaCost() == null) {
                continue;
            }

            ManaCost cost = new ManaCost(card.getManaCost());
            if (cost.hasX()) {
                if (!cost.canPay(virtualPool, 1)) continue;
            } else {
                if (!cost.canPay(virtualPool)) continue;
            }
            int score = scoreCard(gameData, card);
            castableSpells.add(new int[]{i, score});
        }

        if (castableSpells.isEmpty()) {
            return false;
        }

        // Cast the highest-scored spell
        castableSpells.sort((a, b) -> Integer.compare(b[1], a[1]));
        int cardIndex = castableSpells.get(0)[0];
        Card card = hand.get(cardIndex);

        // Determine target if needed
        UUID targetId = null;
        if (card.isNeedsTarget() || card.isAura()) {
            targetId = targetSelector.chooseTarget(gameData, card, aiPlayer.getId());
            if (targetId == null) {
                return false;
            }
        }

        // Calculate X value and tap lands
        ManaCost castCost = new ManaCost(card.getManaCost());
        Integer xValue = null;
        if (castCost.hasX()) {
            int smartX = manaManager.calculateSmartX(gameData, card, targetId, virtualPool);
            if (smartX <= 0) {
                return false;
            }
            xValue = smartX;
            manaManager.tapLandsForXSpell(gameData, aiPlayer.getId(), card, smartX, tapPermanentAction());
        } else {
            manaManager.tapLandsForCost(gameData, aiPlayer.getId(), card.getManaCost(), tapPermanentAction());
        }

        log.info("AI: Casting {}{} in game {}", card.getName(),
                xValue != null ? " (X=" + xValue + ")" : "", gameId);
        final UUID finalTargetId = targetId;
        final Integer finalXValue = xValue;
        send(() -> messageHandler.handlePlayCard(selfConnection,
                new PlayCardRequest(cardIndex, finalXValue, finalTargetId, null, null, null, null, null, null, null)));
        return true;
    }

    protected int scoreCard(GameData gameData, Card card) {
        int score = card.getManaValue() * 10;

        if (card.getType() == CardType.CREATURE) {
            int power = card.getPower() != null ? card.getPower() : 0;
            int toughness = card.getToughness() != null ? card.getToughness() : 0;
            score += (power + toughness) * 5;

            if (card.getKeywords().contains(Keyword.FLYING)) score += 15;
            if (card.getKeywords().contains(Keyword.FIRST_STRIKE)) score += 10;
            if (card.getKeywords().contains(Keyword.DOUBLE_STRIKE)) score += 20;
            if (card.getKeywords().contains(Keyword.TRAMPLE)) score += 10;
            if (card.getKeywords().contains(Keyword.VIGILANCE)) score += 5;
        } else if (card.getType() == CardType.ENCHANTMENT) {
            score += 20;
        }

        return score;
    }

    // ===== Combat =====

    @Override
    protected void handleAttackers(GameData gameData) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(aiPlayer.getId());
        if (battlefield == null) {
            send(() -> messageHandler.handleDeclareAttackers(selfConnection, new DeclareAttackersRequest(List.of(), null)));
            return;
        }

        UUID opponentId = AiUtils.getOpponentId(gameData, aiPlayer.getId());
        List<Permanent> opponentBattlefield = gameData.playerBattlefields.getOrDefault(opponentId, List.of());

        List<Integer> attackerIndices = new ArrayList<>();
        int totalAttackDamage = 0;
        int opponentLife = gameData.getLife(opponentId);

        for (int i = 0; i < battlefield.size(); i++) {
            Permanent perm = battlefield.get(i);
            if (!gameQueryService.isCreature(gameData, perm)) continue;
            if (perm.isTapped()) continue;
            if (perm.isSummoningSick() && !gameQueryService.hasKeyword(gameData, perm, Keyword.HASTE)) continue;
            if (gameQueryService.hasKeyword(gameData, perm, Keyword.DEFENDER)) continue;

            int power = gameQueryService.getEffectivePower(gameData, perm);
            int toughness = gameQueryService.getEffectiveToughness(gameData, perm);
            boolean hasFlying = gameQueryService.hasKeyword(gameData, perm, Keyword.FLYING);

            Permanent bestBlocker = findBestBlocker(gameData, perm, opponentBattlefield);

            boolean shouldAttack = false;
            if (bestBlocker == null) {
                shouldAttack = true;
            } else {
                int blockerPower = gameQueryService.getEffectivePower(gameData, bestBlocker);
                int blockerToughness = gameQueryService.getEffectiveToughness(gameData, bestBlocker);

                if (power >= blockerToughness && blockerPower < toughness) {
                    shouldAttack = true;
                } else if (power >= blockerToughness && perm.getCard().getManaValue() < bestBlocker.getCard().getManaValue()) {
                    shouldAttack = true;
                } else if (hasFlying && !gameQueryService.hasKeyword(gameData, bestBlocker, Keyword.FLYING)
                        && !gameQueryService.hasKeyword(gameData, bestBlocker, Keyword.REACH)) {
                    shouldAttack = true;
                }
            }

            if (shouldAttack) {
                attackerIndices.add(i);
                totalAttackDamage += power;
            }
        }

        // Always attack if lethal
        if (totalAttackDamage < opponentLife) {
            int allInDamage = 0;
            List<Integer> allInIndices = new ArrayList<>();
            for (int i = 0; i < battlefield.size(); i++) {
                Permanent perm = battlefield.get(i);
                if (!gameQueryService.isCreature(gameData, perm)) continue;
                if (perm.isTapped()) continue;
                if (perm.isSummoningSick() && !gameQueryService.hasKeyword(gameData, perm, Keyword.VIGILANCE)
                        && !gameQueryService.hasKeyword(gameData, perm, Keyword.DOUBLE_STRIKE)) continue;
                if (gameQueryService.hasKeyword(gameData, perm, Keyword.DEFENDER)) continue;

                allInDamage += gameQueryService.getEffectivePower(gameData, perm);
                allInIndices.add(i);
            }
            if (allInDamage >= opponentLife) {
                attackerIndices = allInIndices;
            }
        }

        log.info("AI: Declaring {} attackers in game {}", attackerIndices.size(), gameId);
        final List<Integer> finalAttackerIndices = attackerIndices;
        send(() -> messageHandler.handleDeclareAttackers(selfConnection, new DeclareAttackersRequest(finalAttackerIndices, null)));
    }

    @Override
    protected void handleBlockers(GameData gameData) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(aiPlayer.getId());
        UUID opponentId = AiUtils.getOpponentId(gameData, aiPlayer.getId());
        List<Permanent> opponentBattlefield = gameData.playerBattlefields.getOrDefault(opponentId, List.of());

        if (battlefield == null || opponentBattlefield == null) {
            send(() -> messageHandler.handleDeclareBlockers(selfConnection, new DeclareBlockersRequest(List.of())));
            return;
        }

        // Find all attacking creatures that can be blocked
        List<int[]> attackers = new ArrayList<>();
        for (int i = 0; i < opponentBattlefield.size(); i++) {
            Permanent perm = opponentBattlefield.get(i);
            if (perm.isAttacking()) {
                if (gameQueryService.hasCantBeBlocked(gameData, perm)) continue;

                attackers.add(new int[]{i,
                        gameQueryService.getEffectivePower(gameData, perm),
                        gameQueryService.getEffectiveToughness(gameData, perm)});
            }
        }

        attackers.sort((a, b) -> Integer.compare(b[1], a[1]));

        List<BlockerAssignment> assignments = new ArrayList<>();
        boolean[] blockerUsed = new boolean[battlefield.size()];

        int totalIncomingDamage = attackers.stream().mapToInt(a -> a[1]).sum();
        int myLife = gameData.getLife(aiPlayer.getId());
        boolean lethalIncoming = totalIncomingDamage >= myLife;

        for (int[] attacker : attackers) {
            int attackerIdx = attacker[0];
            int attackerPower = attacker[1];
            int attackerToughness = attacker[2];
            Permanent attackingPerm = opponentBattlefield.get(attackerIdx);
            boolean attackerHasFlying = gameQueryService.hasKeyword(gameData, attackingPerm, Keyword.FLYING);
            boolean attackerHasMenace = gameQueryService.hasKeyword(gameData, attackingPerm, Keyword.MENACE);
            List<Integer> availableBlockers = getAvailableBlockersForAttacker(
                    gameData, battlefield, blockerUsed, attackingPerm, attackerHasFlying
            );

            // Find cheapest blocker that can kill attacker and survive
            int bestBlockerIdx = -1;
            int bestBlockerValue = Integer.MAX_VALUE;

            for (int j : availableBlockers) {
                Permanent blocker = battlefield.get(j);
                int blockerPower = gameQueryService.getEffectivePower(gameData, blocker);
                int blockerToughness = gameQueryService.getEffectiveToughness(gameData, blocker);

                if (blockerPower >= attackerToughness && attackerPower < blockerToughness) {
                    int value = blocker.getCard().getManaValue();
                    if (value < bestBlockerValue) {
                        bestBlockerIdx = j;
                        bestBlockerValue = value;
                    }
                }
            }

            List<Integer> bestPair = selectBestFavorablePair(gameData, battlefield, availableBlockers, attackerPower, attackerToughness);

            if (attackerHasMenace) {
                if (bestPair != null) {
                    assignments.add(new BlockerAssignment(bestPair.get(0), attackerIdx));
                    assignments.add(new BlockerAssignment(bestPair.get(1), attackerIdx));
                    blockerUsed[bestPair.get(0)] = true;
                    blockerUsed[bestPair.get(1)] = true;
                    totalIncomingDamage -= attackerPower;
                    lethalIncoming = totalIncomingDamage >= myLife;
                } else if (lethalIncoming && availableBlockers.size() >= 2) {
                    List<Integer> chumpPair = pickCheapestBlockers(battlefield, availableBlockers, 2);
                    assignments.add(new BlockerAssignment(chumpPair.get(0), attackerIdx));
                    assignments.add(new BlockerAssignment(chumpPair.get(1), attackerIdx));
                    blockerUsed[chumpPair.get(0)] = true;
                    blockerUsed[chumpPair.get(1)] = true;
                    totalIncomingDamage -= attackerPower;
                    lethalIncoming = totalIncomingDamage >= myLife;
                }
                continue;
            }

            if (bestBlockerIdx != -1) {
                assignments.add(new BlockerAssignment(bestBlockerIdx, attackerIdx));
                blockerUsed[bestBlockerIdx] = true;
                totalIncomingDamage -= attackerPower;
                lethalIncoming = totalIncomingDamage >= myLife;
            } else if (bestPair != null) {
                assignments.add(new BlockerAssignment(bestPair.get(0), attackerIdx));
                assignments.add(new BlockerAssignment(bestPair.get(1), attackerIdx));
                blockerUsed[bestPair.get(0)] = true;
                blockerUsed[bestPair.get(1)] = true;
                totalIncomingDamage -= attackerPower;
                lethalIncoming = totalIncomingDamage >= myLife;
            } else if (lethalIncoming) {
                for (int j : availableBlockers) {
                    assignments.add(new BlockerAssignment(j, attackerIdx));
                    blockerUsed[j] = true;
                    totalIncomingDamage -= attackerPower;
                    lethalIncoming = totalIncomingDamage >= myLife;
                    break;
                }
            }
        }

        log.info("AI: Declaring {} blockers in game {}", assignments.size(), gameId);
        send(() -> messageHandler.handleDeclareBlockers(selfConnection, new DeclareBlockersRequest(assignments)));
    }

    // ===== Combat Helpers =====

    private Permanent findBestBlocker(GameData gameData, Permanent attacker, List<Permanent> opponentField) {
        boolean hasFlying = gameQueryService.hasKeyword(gameData, attacker, Keyword.FLYING);

        Permanent best = null;
        int bestToughness = Integer.MAX_VALUE;

        for (Permanent opp : opponentField) {
            if (!gameQueryService.isCreature(gameData, opp)) continue;
            if (opp.isTapped()) continue;

            if (hasFlying && !gameQueryService.hasKeyword(gameData, opp, Keyword.FLYING)
                    && !gameQueryService.hasKeyword(gameData, opp, Keyword.REACH)) {
                continue;
            }

            int oppToughness = gameQueryService.getEffectiveToughness(gameData, opp);
            if (best == null || oppToughness < bestToughness) {
                best = opp;
                bestToughness = oppToughness;
            }
        }
        return best;
    }

    private List<Integer> getAvailableBlockersForAttacker(GameData gameData, List<Permanent> battlefield, boolean[] blockerUsed,
                                                          Permanent attackingPerm, boolean attackerHasFlying) {
        List<Integer> available = new ArrayList<>();
        for (int j = 0; j < battlefield.size(); j++) {
            if (blockerUsed[j]) continue;
            Permanent blocker = battlefield.get(j);
            if (!gameQueryService.isCreature(gameData, blocker)) continue;
            if (blocker.isTapped()) continue;

            if (attackerHasFlying && !gameQueryService.hasKeyword(gameData, blocker, Keyword.FLYING)
                    && !gameQueryService.hasKeyword(gameData, blocker, Keyword.REACH)) {
                continue;
            }
            available.add(j);
        }
        return available;
    }

    private List<Integer> selectBestFavorablePair(GameData gameData, List<Permanent> battlefield, List<Integer> availableBlockers,
                                                   int attackerPower, int attackerToughness) {
        List<Integer> bestPair = null;
        int bestPairValue = Integer.MAX_VALUE;

        for (int a = 0; a < availableBlockers.size(); a++) {
            for (int b = a + 1; b < availableBlockers.size(); b++) {
                int idxA = availableBlockers.get(a);
                int idxB = availableBlockers.get(b);
                Permanent blockerA = battlefield.get(idxA);
                Permanent blockerB = battlefield.get(idxB);

                int powerA = gameQueryService.getEffectivePower(gameData, blockerA);
                int powerB = gameQueryService.getEffectivePower(gameData, blockerB);
                int toughnessA = gameQueryService.getEffectiveToughness(gameData, blockerA);
                int toughnessB = gameQueryService.getEffectiveToughness(gameData, blockerB);

                boolean killsAttacker = powerA + powerB >= attackerToughness;
                boolean oneSurvives = attackerPower < toughnessA || attackerPower < toughnessB;
                if (!killsAttacker || !oneSurvives) continue;

                int value = blockerA.getCard().getManaValue() + blockerB.getCard().getManaValue();
                if (value < bestPairValue) {
                    bestPair = List.of(idxA, idxB);
                    bestPairValue = value;
                }
            }
        }
        return bestPair;
    }

    private List<Integer> pickCheapestBlockers(List<Permanent> battlefield, List<Integer> availableBlockers, int count) {
        return availableBlockers.stream()
                .sorted(Comparator.comparingInt(idx -> battlefield.get(idx).getCard().getManaValue()))
                .limit(count)
                .toList();
    }
}
