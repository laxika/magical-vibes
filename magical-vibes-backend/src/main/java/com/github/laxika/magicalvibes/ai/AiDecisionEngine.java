package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.BoostEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.GameService;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
public class AiDecisionEngine {

    private final UUID gameId;
    private final Player aiPlayer;
    private final GameRegistry gameRegistry;
    private final GameService gameService;

    public AiDecisionEngine(UUID gameId, Player aiPlayer, GameRegistry gameRegistry, GameService gameService) {
        this.gameId = gameId;
        this.aiPlayer = aiPlayer;
        this.gameRegistry = gameRegistry;
        this.gameService = gameService;
    }

    public void handleMessage(String type, String json) {
        GameData gameData = gameRegistry.get(gameId);
        if (gameData == null || gameData.status == GameStatus.FINISHED) {
            return;
        }

        switch (type) {
            case "GAME_STATE" -> handleGameState(gameData);
            case "MULLIGAN_RESOLVED" -> handleMulliganResolved(gameData);
            case "SELECT_CARDS_TO_BOTTOM" -> handleBottomCards(gameData);
            case "AVAILABLE_ATTACKERS" -> handleAttackers(gameData);
            case "AVAILABLE_BLOCKERS" -> handleBlockers(gameData);
            case "CHOOSE_CARD_FROM_HAND" -> handleCardChoice(gameData);
            case "CHOOSE_PERMANENT" -> handlePermanentChoice(gameData);
            case "CHOOSE_MULTIPLE_PERMANENTS" -> handleMultiPermanentChoice(gameData);
            case "CHOOSE_COLOR" -> handleColorChoice(gameData);
            case "MAY_ABILITY_CHOICE" -> handleMayAbilityChoice(gameData);
            case "REORDER_LIBRARY_CARDS" -> handleReorderCards(gameData);
            case "CHOOSE_CARD_FROM_LIBRARY" -> handleLibrarySearch(gameData);
            case "CHOOSE_CARD_FROM_GRAVEYARD" -> handleGraveyardChoice(gameData);
            case "CHOOSE_MULTIPLE_CARDS_FROM_GRAVEYARDS" -> handleMultiGraveyardChoice(gameData);
            case "CHOOSE_HAND_TOP_BOTTOM" -> handleHandTopBottom(gameData);
            case "CHOOSE_FROM_REVEALED_HAND" -> handleRevealedHandChoice(gameData);
            case "GAME_OVER" -> log.info("AI: Game {} is over", gameId);
            default -> {
                // Ignore informational messages (BATTLEFIELD_UPDATED, MANA_UPDATED, etc.)
            }
        }
    }

    // ===== Mulligan =====

    public void handleInitialMulligan(GameData gameData) {
        if (shouldKeepHand(gameData)) {
            log.info("AI: Keeping hand in game {}", gameId);
            gameService.keepHand(gameData, aiPlayer);
        } else {
            log.info("AI: Taking mulligan in game {}", gameId);
            gameService.mulligan(gameData, aiPlayer);
        }
    }

    private void handleMulliganResolved(GameData gameData) {
        if (gameData.playerKeptHand.contains(aiPlayer.getId())) {
            return;
        }
        handleInitialMulligan(gameData);
    }

    private boolean shouldKeepHand(GameData gameData) {
        List<Card> hand = gameData.playerHands.get(aiPlayer.getId());
        if (hand == null || hand.isEmpty()) {
            return true;
        }

        int mulliganCount = gameData.mulliganCounts.getOrDefault(aiPlayer.getId(), 0);

        // Always keep after 3 mulligans
        if (mulliganCount >= 3) {
            return true;
        }

        long landCount = hand.stream().filter(c -> c.getType() == CardType.LAND).count();

        // Relax land requirement as mulligans increase
        if (mulliganCount >= 2) {
            return landCount >= 1;
        }
        if (mulliganCount >= 1) {
            return landCount >= 1 && landCount <= 5;
        }

        return landCount >= 2 && landCount <= 5;
    }

    private void handleBottomCards(GameData gameData) {
        Integer needsToBottom = gameData.playerNeedsToBottom.get(aiPlayer.getId());
        if (needsToBottom == null || needsToBottom <= 0) {
            return;
        }

        List<Card> hand = gameData.playerHands.get(aiPlayer.getId());
        if (hand == null) {
            return;
        }

        // Score each card — higher score means we want to bottom it
        List<int[]> scoredIndices = new ArrayList<>();
        long landCount = hand.stream().filter(c -> c.getType() == CardType.LAND).count();
        long spellCount = hand.size() - landCount;

        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            int score;
            if (card.getType() == CardType.LAND) {
                // Bottom excess lands first (keep at least 2)
                score = landCount > 2 ? 1000 : -1000;
                if (landCount > 2) {
                    landCount--;
                }
            } else {
                // For non-lands, bottom highest mana cost first
                score = card.getManaValue();
            }
            scoredIndices.add(new int[]{i, score});
        }

        scoredIndices.sort((a, b) -> Integer.compare(b[1], a[1]));

        List<Integer> toBottom = new ArrayList<>();
        for (int i = 0; i < needsToBottom && i < scoredIndices.size(); i++) {
            toBottom.add(scoredIndices.get(i)[0]);
        }

        log.info("AI: Bottoming {} cards in game {}", toBottom.size(), gameId);
        gameService.bottomCards(gameData, aiPlayer, toBottom);
    }

    // ===== Priority / Main Phase =====

    private void handleGameState(GameData gameData) {
        if (gameData.status != GameStatus.RUNNING) {
            return;
        }

        // Check if AI has priority
        UUID priorityHolder = getPriorityPlayerId(gameData);
        if (priorityHolder == null || !priorityHolder.equals(aiPlayer.getId())) {
            return;
        }

        // If awaiting some input, don't try to act on priority
        if (gameData.awaitingInput != null) {
            return;
        }

        boolean isMainPhase = gameData.currentStep == TurnStep.PRECOMBAT_MAIN
                || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN;
        boolean isActivePlayer = aiPlayer.getId().equals(gameData.activePlayerId);

        if (isMainPhase && isActivePlayer && gameData.stack.isEmpty()) {
            // Try to play a land
            if (tryPlayLand(gameData)) {
                return;
            }

            // Try to cast a spell
            if (tryCastSpell(gameData)) {
                return;
            }
        }

        // Pass priority
        gameService.passPriority(gameData, aiPlayer);
    }

    private boolean tryPlayLand(GameData gameData) {
        int landsPlayed = gameData.landsPlayedThisTurn.getOrDefault(aiPlayer.getId(), 0);
        if (landsPlayed > 0) {
            return false;
        }

        List<Card> hand = gameData.playerHands.get(aiPlayer.getId());
        if (hand == null) {
            return false;
        }

        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (card.getType() == CardType.LAND) {
                log.info("AI: Playing land {} in game {}", card.getName(), gameId);
                gameService.playCard(gameData, aiPlayer, i, null, null, null);
                return true;
            }
        }
        return false;
    }

    private boolean tryCastSpell(GameData gameData) {
        List<Card> hand = gameData.playerHands.get(aiPlayer.getId());
        if (hand == null) {
            return false;
        }

        // Build virtual mana pool (current pool + all untapped mana sources)
        ManaPool virtualPool = buildVirtualManaPool(gameData);

        // Score and sort castable spells
        List<int[]> castableSpells = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (card.getType() == CardType.LAND) {
                continue;
            }
            // Can only cast sorceries/creatures/enchantments/artifacts during main phase as active player
            if (card.getType() == CardType.INSTANT) {
                continue; // AI won't cast instants during its own main phase for simplicity
            }
            if (card.getManaCost() == null) {
                continue;
            }

            ManaCost cost = new ManaCost(card.getManaCost());
            if (cost.canPay(virtualPool)) {
                int score = scoreCard(gameData, card);
                castableSpells.add(new int[]{i, score});
            }
        }

        if (castableSpells.isEmpty()) {
            return false;
        }

        // Cast the highest-scored spell
        castableSpells.sort((a, b) -> Integer.compare(b[1], a[1]));
        int cardIndex = castableSpells.get(0)[0];
        Card card = hand.get(cardIndex);

        // Determine target if needed (before tapping lands, so we don't waste mana)
        UUID targetId = null;
        if (card.isNeedsTarget() || card.isAura()) {
            targetId = chooseTarget(gameData, card);
            if (targetId == null) {
                // Can't find a valid target, don't cast
                return false;
            }
        }

        // Tap lands to pay for it
        tapLandsForCost(gameData, card.getManaCost());

        log.info("AI: Casting {} in game {}", card.getName(), gameId);
        gameService.playCard(gameData, aiPlayer, cardIndex, null, targetId, null);
        return true;
    }

    private ManaPool buildVirtualManaPool(GameData gameData) {
        ManaPool virtual = new ManaPool();

        // Copy current pool
        ManaPool current = gameData.playerManaPools.get(aiPlayer.getId());
        if (current != null) {
            for (ManaColor color : ManaColor.values()) {
                for (int i = 0; i < current.get(color); i++) {
                    virtual.add(color);
                }
            }
        }

        // Add mana from untapped lands/mana sources
        List<Permanent> battlefield = gameData.playerBattlefields.get(aiPlayer.getId());
        if (battlefield != null) {
            for (Permanent perm : battlefield) {
                if (perm.isTapped()) {
                    continue;
                }
                // Skip creatures with summoning sickness (they can't tap)
                if (gameService.isCreature(gameData, perm) && perm.isSummoningSick()
                        && !gameService.hasKeyword(gameData, perm, Keyword.HASTE)) {
                    continue;
                }
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_TAP)) {
                    if (effect instanceof AwardManaEffect manaEffect) {
                        virtual.add(manaEffect.color());
                    } else if (effect instanceof AwardAnyColorManaEffect) {
                        // Conservatively add colorless — it can pay generic costs
                        virtual.add(ManaColor.COLORLESS);
                    }
                }
            }
        }

        return virtual;
    }

    private void tapLandsForCost(GameData gameData, String manaCostStr) {
        ManaCost cost = new ManaCost(manaCostStr);
        ManaPool currentPool = gameData.playerManaPools.get(aiPlayer.getId());

        if (cost.canPay(currentPool)) {
            return; // Already have enough mana
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(aiPlayer.getId());
        if (battlefield == null) {
            return;
        }

        for (int i = 0; i < battlefield.size(); i++) {
            Permanent perm = battlefield.get(i);
            if (perm.isTapped()) {
                continue;
            }

            // Skip creatures with summoning sickness
            if (gameService.isCreature(gameData, perm) && perm.isSummoningSick()
                    && !gameService.hasKeyword(gameData, perm, Keyword.HASTE)) {
                continue;
            }

            boolean producesMana = perm.getCard().getEffects(EffectSlot.ON_TAP).stream()
                    .anyMatch(e -> e instanceof AwardManaEffect || e instanceof AwardAnyColorManaEffect);
            if (!producesMana) {
                continue;
            }

            gameService.tapPermanent(gameData, aiPlayer, i);

            // Re-read pool after tap (mutated in place)
            currentPool = gameData.playerManaPools.get(aiPlayer.getId());
            if (cost.canPay(currentPool)) {
                return;
            }
        }
    }

    private int scoreCard(GameData gameData, Card card) {
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

    private UUID chooseTarget(GameData gameData, Card card) {
        UUID opponentId = getOpponentId(gameData);

        boolean isBeneficial = false;
        if (card.isAura()) {
            for (CardEffect effect : card.getEffects(EffectSlot.STATIC)) {
                if (effect instanceof BoostEnchantedCreatureEffect || effect instanceof GrantKeywordToEnchantedCreatureEffect) {
                    isBeneficial = true;
                    break;
                }
            }
        }

        if (isBeneficial) {
            // Target own creature with highest toughness
            List<Permanent> ownBattlefield = gameData.playerBattlefields.get(aiPlayer.getId());
            if (ownBattlefield != null) {
                return ownBattlefield.stream()
                        .filter(p -> gameService.isCreature(gameData, p))
                        .max(Comparator.comparingInt(p -> gameService.getEffectiveToughness(gameData, p)))
                        .map(Permanent::getId)
                        .orElse(null);
            }
        } else {
            // Detrimental — target opponent's highest-power creature that doesn't already have this effect
            List<Permanent> oppBattlefield = gameData.playerBattlefields.get(opponentId);
            if (oppBattlefield != null) {
                List<Class<? extends CardEffect>> auraEffectClasses = card.getEffects(EffectSlot.STATIC).stream()
                        .map(CardEffect::getClass)
                        .toList();
                return oppBattlefield.stream()
                        .filter(p -> gameService.isCreature(gameData, p))
                        .filter(p -> auraEffectClasses.stream().noneMatch(ec -> gameService.hasAuraWithEffect(gameData, p, ec)))
                        .max(Comparator.comparingInt(p -> gameService.getEffectivePower(gameData, p)))
                        .map(Permanent::getId)
                        .orElse(null);
            }
        }
        return null;
    }

    // ===== Combat =====

    private void handleAttackers(GameData gameData) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(aiPlayer.getId());
        if (battlefield == null) {
            gameService.declareAttackers(gameData, aiPlayer, List.of());
            return;
        }

        UUID opponentId = getOpponentId(gameData);
        List<Permanent> opponentBattlefield = gameData.playerBattlefields.getOrDefault(opponentId, List.of());

        List<Integer> attackerIndices = new ArrayList<>();
        int totalAttackDamage = 0;
        int opponentLife = gameData.playerLifeTotals.getOrDefault(opponentId, 20);

        for (int i = 0; i < battlefield.size(); i++) {
            Permanent perm = battlefield.get(i);
            if (!gameService.isCreature(gameData, perm)) continue;
            if (perm.isTapped()) continue;
            if (perm.isSummoningSick() && !gameService.hasKeyword(gameData, perm, Keyword.HASTE)) continue;
            if (gameService.hasKeyword(gameData, perm, Keyword.DEFENDER)) continue;

            int power = gameService.getEffectivePower(gameData, perm);
            int toughness = gameService.getEffectiveToughness(gameData, perm);
            boolean hasFlying = gameService.hasKeyword(gameData, perm, Keyword.FLYING);

            // Find the best potential blocker
            Permanent bestBlocker = findBestBlocker(gameData, perm, opponentBattlefield);

            boolean shouldAttack = false;
            if (bestBlocker == null) {
                // No blocker available — safe to attack
                shouldAttack = true;
            } else {
                int blockerPower = gameService.getEffectivePower(gameData, bestBlocker);
                int blockerToughness = gameService.getEffectiveToughness(gameData, bestBlocker);

                // Attack if we kill the blocker and survive
                if (power >= blockerToughness && blockerPower < toughness) {
                    shouldAttack = true;
                }
                // Attack if favorable mana-value trade (we kill them, even if we die too)
                else if (power >= blockerToughness && perm.getCard().getManaValue() < bestBlocker.getCard().getManaValue()) {
                    shouldAttack = true;
                }
                // Attack with evasion creatures (flying that can't be blocked by reach-less ground)
                else if (hasFlying && !gameService.hasKeyword(gameData, bestBlocker, Keyword.FLYING)
                        && !gameService.hasKeyword(gameData, bestBlocker, Keyword.REACH)) {
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
            // Check if all-in attack is lethal
            int allInDamage = 0;
            List<Integer> allInIndices = new ArrayList<>();
            for (int i = 0; i < battlefield.size(); i++) {
                Permanent perm = battlefield.get(i);
                if (!gameService.isCreature(gameData, perm)) continue;
                if (perm.isTapped()) continue;
                if (perm.isSummoningSick() && !gameService.hasKeyword(gameData, perm, Keyword.VIGILANCE)
                        && !gameService.hasKeyword(gameData, perm, Keyword.DOUBLE_STRIKE)) continue;
                if (gameService.hasKeyword(gameData, perm, Keyword.DEFENDER)) continue;

                allInDamage += gameService.getEffectivePower(gameData, perm);
                allInIndices.add(i);
            }
            if (allInDamage >= opponentLife) {
                attackerIndices = allInIndices;
            }
        }

        log.info("AI: Declaring {} attackers in game {}", attackerIndices.size(), gameId);
        gameService.declareAttackers(gameData, aiPlayer, attackerIndices);
    }

    private Permanent findBestBlocker(GameData gameData, Permanent attacker, List<Permanent> opponentField) {
        boolean hasFlying = gameService.hasKeyword(gameData, attacker, Keyword.FLYING);

        Permanent best = null;
        int bestToughness = Integer.MAX_VALUE;

        for (Permanent opp : opponentField) {
            if (!gameService.isCreature(gameData, opp)) continue;
            if (opp.isTapped()) continue;
            if (gameService.hasKeyword(gameData, opp, Keyword.DEFENDER) || !opp.isSummoningSick() || true) {
                // Can potentially block
            }

            // Flying creatures can only be blocked by flying or reach
            if (hasFlying && !gameService.hasKeyword(gameData, opp, Keyword.FLYING)
                    && !gameService.hasKeyword(gameData, opp, Keyword.REACH)) {
                continue;
            }

            int oppToughness = gameService.getEffectiveToughness(gameData, opp);
            if (best == null || oppToughness < bestToughness) {
                best = opp;
                bestToughness = oppToughness;
            }
        }
        return best;
    }

    private void handleBlockers(GameData gameData) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(aiPlayer.getId());
        UUID opponentId = getOpponentId(gameData);
        List<Permanent> opponentBattlefield = gameData.playerBattlefields.getOrDefault(opponentId, List.of());

        if (battlefield == null || opponentBattlefield == null) {
            gameService.declareBlockers(gameData, aiPlayer, List.of());
            return;
        }

        // Find all attacking creatures that can be blocked
        List<int[]> attackers = new ArrayList<>(); // [index in opponent field, power, toughness]
        for (int i = 0; i < opponentBattlefield.size(); i++) {
            Permanent perm = opponentBattlefield.get(i);
            if (perm.isAttacking()) {
                // Skip unblockable creatures
                if (perm.isCantBeBlocked()) continue;
                boolean hasCantBeBlockedStatic = perm.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(e -> e instanceof CantBeBlockedEffect);
                if (hasCantBeBlockedStatic) continue;

                attackers.add(new int[]{i,
                        gameService.getEffectivePower(gameData, perm),
                        gameService.getEffectiveToughness(gameData, perm)});
            }
        }

        // Sort attackers by power descending (block biggest threats first)
        attackers.sort((a, b) -> Integer.compare(b[1], a[1]));

        List<BlockerAssignment> assignments = new ArrayList<>();
        boolean[] blockerUsed = new boolean[battlefield.size()];

        // Calculate total incoming damage
        int totalIncomingDamage = attackers.stream().mapToInt(a -> a[1]).sum();
        int myLife = gameData.playerLifeTotals.getOrDefault(aiPlayer.getId(), 20);
        boolean lethalIncoming = totalIncomingDamage >= myLife;

        for (int[] attacker : attackers) {
            int attackerIdx = attacker[0];
            int attackerPower = attacker[1];
            int attackerToughness = attacker[2];
            Permanent attackingPerm = opponentBattlefield.get(attackerIdx);
            boolean attackerHasFlying = gameService.hasKeyword(gameData, attackingPerm, Keyword.FLYING);

            // Find cheapest blocker that can kill attacker and survive
            int bestBlockerIdx = -1;
            int bestBlockerValue = Integer.MAX_VALUE;

            for (int j = 0; j < battlefield.size(); j++) {
                if (blockerUsed[j]) continue;
                Permanent blocker = battlefield.get(j);
                if (!gameService.isCreature(gameData, blocker)) continue;
                if (blocker.isTapped()) continue;

                // Flying can only be blocked by flying or reach
                if (attackerHasFlying && !gameService.hasKeyword(gameData, blocker, Keyword.FLYING)
                        && !gameService.hasKeyword(gameData, blocker, Keyword.REACH)) {
                    continue;
                }

                int blockerPower = gameService.getEffectivePower(gameData, blocker);
                int blockerToughness = gameService.getEffectiveToughness(gameData, blocker);

                // Favorable block: we kill attacker and survive
                if (blockerPower >= attackerToughness && attackerPower < blockerToughness) {
                    int value = blocker.getCard().getManaValue();
                    if (value < bestBlockerValue) {
                        bestBlockerIdx = j;
                        bestBlockerValue = value;
                    }
                }
            }

            if (bestBlockerIdx != -1) {
                assignments.add(new BlockerAssignment(bestBlockerIdx, attackerIdx));
                blockerUsed[bestBlockerIdx] = true;
            } else if (lethalIncoming) {
                // Chump block to survive
                for (int j = 0; j < battlefield.size(); j++) {
                    if (blockerUsed[j]) continue;
                    Permanent blocker = battlefield.get(j);
                    if (!gameService.isCreature(gameData, blocker)) continue;
                    if (blocker.isTapped()) continue;

                    if (attackerHasFlying && !gameService.hasKeyword(gameData, blocker, Keyword.FLYING)
                            && !gameService.hasKeyword(gameData, blocker, Keyword.REACH)) {
                        continue;
                    }

                    assignments.add(new BlockerAssignment(j, attackerIdx));
                    blockerUsed[j] = true;
                    totalIncomingDamage -= attackerPower;
                    lethalIncoming = totalIncomingDamage >= myLife;
                    break;
                }
            }
        }

        log.info("AI: Declaring {} blockers in game {}", assignments.size(), gameId);
        gameService.declareBlockers(gameData, aiPlayer, assignments);
    }

    // ===== Choice Handlers =====

    private void handleCardChoice(GameData gameData) {
        // Discard: pick highest mana cost card
        if (!aiPlayer.getId().equals(gameData.awaitingCardChoicePlayerId)) {
            return;
        }

        List<Card> hand = gameData.playerHands.get(aiPlayer.getId());
        Set<Integer> validIndices = gameData.awaitingCardChoiceValidIndices;
        if (hand == null || validIndices == null || validIndices.isEmpty()) {
            return;
        }

        int bestIndex = validIndices.stream()
                .max(Comparator.comparingInt(i -> hand.get(i).getManaValue()))
                .orElse(validIndices.iterator().next());

        log.info("AI: Choosing card at index {} in game {}", bestIndex, gameId);
        gameService.handleCardChosen(gameData, aiPlayer, bestIndex);
    }

    private void handlePermanentChoice(GameData gameData) {
        if (!aiPlayer.getId().equals(gameData.awaitingPermanentChoicePlayerId)) {
            return;
        }

        Set<UUID> validIds = gameData.awaitingPermanentChoiceValidIds;
        if (validIds == null || validIds.isEmpty()) {
            return;
        }

        // Pick opponent's biggest threat, or own weakest
        UUID opponentId = getOpponentId(gameData);
        List<Permanent> opponentField = gameData.playerBattlefields.getOrDefault(opponentId, List.of());
        List<Permanent> ownField = gameData.playerBattlefields.getOrDefault(aiPlayer.getId(), List.of());

        // Try opponent's best creature first
        UUID best = opponentField.stream()
                .filter(p -> validIds.contains(p.getId()))
                .filter(p -> gameService.isCreature(gameData, p))
                .max(Comparator.comparingInt(p -> gameService.getEffectivePower(gameData, p)))
                .map(Permanent::getId)
                .orElse(null);

        if (best == null) {
            // Try any opponent permanent
            best = opponentField.stream()
                    .filter(p -> validIds.contains(p.getId()))
                    .max(Comparator.comparingInt(p -> p.getCard().getManaValue()))
                    .map(Permanent::getId)
                    .orElse(null);
        }

        if (best == null) {
            // Fall back to own weakest permanent
            best = ownField.stream()
                    .filter(p -> validIds.contains(p.getId()))
                    .min(Comparator.comparingInt(p -> p.getCard().getManaValue()))
                    .map(Permanent::getId)
                    .orElse(validIds.iterator().next());
        }

        log.info("AI: Choosing permanent {} in game {}", best, gameId);
        gameService.handlePermanentChosen(gameData, aiPlayer, best);
    }

    private void handleMultiPermanentChoice(GameData gameData) {
        if (!aiPlayer.getId().equals(gameData.awaitingMultiPermanentChoicePlayerId)) {
            return;
        }

        Set<UUID> validIds = gameData.awaitingMultiPermanentChoiceValidIds;
        int maxCount = gameData.awaitingMultiPermanentChoiceMaxCount;
        if (validIds == null || validIds.isEmpty()) {
            return;
        }

        UUID opponentId = getOpponentId(gameData);
        List<Permanent> opponentField = gameData.playerBattlefields.getOrDefault(opponentId, List.of());

        // Pick opponent's biggest threats
        List<UUID> chosen = opponentField.stream()
                .filter(p -> validIds.contains(p.getId()))
                .sorted(Comparator.comparingInt((Permanent p) -> gameService.getEffectivePower(gameData, p)).reversed())
                .limit(maxCount)
                .map(Permanent::getId)
                .toList();

        if (chosen.isEmpty()) {
            // Fall back to any valid
            chosen = validIds.stream().limit(maxCount).toList();
        }

        log.info("AI: Choosing {} permanents in game {}", chosen.size(), gameId);
        gameService.handleMultiplePermanentsChosen(gameData, aiPlayer, chosen);
    }

    private void handleColorChoice(GameData gameData) {
        if (!aiPlayer.getId().equals(gameData.awaitingColorChoicePlayerId)) {
            return;
        }

        // Pick the color that appears most on opponent's battlefield
        UUID opponentId = getOpponentId(gameData);
        List<Permanent> opponentField = gameData.playerBattlefields.getOrDefault(opponentId, List.of());

        int[] colorCounts = new int[CardColor.values().length];
        for (Permanent perm : opponentField) {
            CardColor color = perm.getCard().getColor();
            if (color != null) {
                colorCounts[color.ordinal()]++;
            }
        }

        CardColor bestColor = CardColor.WHITE;
        int bestCount = 0;
        for (CardColor color : CardColor.values()) {
            if (colorCounts[color.ordinal()] > bestCount) {
                bestCount = colorCounts[color.ordinal()];
                bestColor = color;
            }
        }

        log.info("AI: Choosing color {} in game {}", bestColor.name(), gameId);
        gameService.handleColorChosen(gameData, aiPlayer, bestColor.name());
    }

    private void handleMayAbilityChoice(GameData gameData) {
        if (!aiPlayer.getId().equals(gameData.awaitingMayAbilityPlayerId)) {
            return;
        }

        // Generally accept may abilities
        log.info("AI: Accepting may ability in game {}", gameId);
        gameService.handleMayAbilityChosen(gameData, aiPlayer, true);
    }

    private void handleReorderCards(GameData gameData) {
        if (!aiPlayer.getId().equals(gameData.awaitingLibraryReorderPlayerId)) {
            return;
        }

        List<Card> cards = gameData.awaitingLibraryReorderCards;
        if (cards == null || cards.isEmpty()) {
            return;
        }

        // Put spells on top, lands on bottom; sort by mana value ascending
        List<int[]> indexedCards = new ArrayList<>();
        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            int priority = card.getType() == CardType.LAND ? 1000 + i : card.getManaValue();
            indexedCards.add(new int[]{i, priority});
        }
        indexedCards.sort(Comparator.comparingInt(a -> a[1]));

        List<Integer> order = indexedCards.stream().map(a -> a[0]).toList();

        log.info("AI: Reordering {} library cards in game {}", order.size(), gameId);
        gameService.handleLibraryCardsReordered(gameData, aiPlayer, order);
    }

    private void handleLibrarySearch(GameData gameData) {
        if (!aiPlayer.getId().equals(gameData.awaitingLibrarySearchPlayerId)) {
            return;
        }

        List<Card> searchCards = gameData.awaitingLibrarySearchCards;
        if (searchCards == null || searchCards.isEmpty()) {
            return;
        }

        // Pick highest value non-land, or first card
        int bestIndex = 0;
        int bestScore = -1;
        for (int i = 0; i < searchCards.size(); i++) {
            Card card = searchCards.get(i);
            int score = card.getType() == CardType.LAND ? card.getManaValue() : card.getManaValue() * 2 + 10;
            if (score > bestScore) {
                bestScore = score;
                bestIndex = i;
            }
        }

        log.info("AI: Choosing card {} from library in game {}", searchCards.get(bestIndex).getName(), gameId);
        gameService.handleLibraryCardChosen(gameData, aiPlayer, bestIndex);
    }

    private void handleGraveyardChoice(GameData gameData) {
        if (!aiPlayer.getId().equals(gameData.awaitingGraveyardChoicePlayerId)) {
            return;
        }

        Set<Integer> validIndices = gameData.awaitingGraveyardChoiceValidIndices;
        if (validIndices == null || validIndices.isEmpty()) {
            return;
        }

        // Pick highest mana value card
        List<Card> graveyard = gameData.graveyardChoiceCardPool;
        if (graveyard == null) {
            graveyard = gameData.playerGraveyards.getOrDefault(aiPlayer.getId(), List.of());
        }

        final List<Card> gy = graveyard;
        int bestIndex = validIndices.stream()
                .max(Comparator.comparingInt(i -> i < gy.size() ? gy.get(i).getManaValue() : 0))
                .orElse(validIndices.iterator().next());

        log.info("AI: Choosing graveyard card at index {} in game {}", bestIndex, gameId);
        gameService.handleGraveyardCardChosen(gameData, aiPlayer, bestIndex);
    }

    private void handleMultiGraveyardChoice(GameData gameData) {
        if (!aiPlayer.getId().equals(gameData.awaitingMultiGraveyardChoicePlayerId)) {
            return;
        }

        Set<UUID> validIds = gameData.awaitingMultiGraveyardChoiceValidCardIds;
        int maxCount = gameData.awaitingMultiGraveyardChoiceMaxCount;
        if (validIds == null || validIds.isEmpty()) {
            return;
        }

        List<UUID> chosen = validIds.stream().limit(maxCount).toList();

        log.info("AI: Choosing {} graveyard cards in game {}", chosen.size(), gameId);
        gameService.handleMultipleGraveyardCardsChosen(gameData, aiPlayer, chosen);
    }

    private void handleHandTopBottom(GameData gameData) {
        if (!aiPlayer.getId().equals(gameData.awaitingHandTopBottomPlayerId)) {
            return;
        }

        List<Card> cards = gameData.awaitingHandTopBottomCards;
        if (cards == null || cards.size() < 2) {
            return;
        }

        // Keep the better card (higher mana value for spells), put land on bottom
        int handCardIndex = 0;
        int topCardIndex = 1;

        Card card0 = cards.get(0);
        Card card1 = cards.get(1);

        // Prefer keeping spells over lands
        boolean card0IsLand = card0.getType() == CardType.LAND;
        boolean card1IsLand = card1.getType() == CardType.LAND;

        if (card0IsLand && !card1IsLand) {
            handCardIndex = 1;
            topCardIndex = 0;
        } else if (!card0IsLand && card1IsLand) {
            handCardIndex = 0;
            topCardIndex = 1;
        } else {
            // Both same type — keep higher value
            if (card1.getManaValue() > card0.getManaValue()) {
                handCardIndex = 1;
                topCardIndex = 0;
            }
        }

        log.info("AI: Choosing hand={} top={} in game {}", handCardIndex, topCardIndex, gameId);
        gameService.handleHandTopBottomChosen(gameData, aiPlayer, handCardIndex, topCardIndex);
    }

    private void handleRevealedHandChoice(GameData gameData) {
        // We're choosing a card from the opponent's revealed hand
        if (!aiPlayer.getId().equals(gameData.awaitingCardChoicePlayerId)) {
            return;
        }

        UUID targetPlayerId = gameData.awaitingRevealedHandChoiceTargetPlayerId;
        if (targetPlayerId == null) {
            return;
        }

        List<Card> targetHand = gameData.playerHands.get(targetPlayerId);
        Set<Integer> validIndices = gameData.awaitingCardChoiceValidIndices;
        if (targetHand == null || validIndices == null || validIndices.isEmpty()) {
            return;
        }

        // Pick highest mana value card from opponent's hand
        int bestIndex = validIndices.stream()
                .max(Comparator.comparingInt(i -> targetHand.get(i).getManaValue()))
                .orElse(validIndices.iterator().next());

        log.info("AI: Choosing card {} from revealed hand in game {}", bestIndex, gameId);
        gameService.handleCardChosen(gameData, aiPlayer, bestIndex);
    }

    // ===== Utility =====

    private UUID getOpponentId(GameData gameData) {
        for (UUID id : gameData.orderedPlayerIds) {
            if (!id.equals(aiPlayer.getId())) {
                return id;
            }
        }
        return null;
    }

    private UUID getPriorityPlayerId(GameData gameData) {
        if (gameData.activePlayerId == null) {
            return null;
        }
        if (!gameData.priorityPassedBy.contains(gameData.activePlayerId)) {
            return gameData.activePlayerId;
        }
        List<UUID> ids = new ArrayList<>(gameData.orderedPlayerIds);
        UUID nonActive = ids.get(0).equals(gameData.activePlayerId) ? ids.get(1) : ids.get(0);
        if (!gameData.priorityPassedBy.contains(nonActive)) {
            return nonActive;
        }
        return null;
    }
}
