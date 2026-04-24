package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectResolution;
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
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedByAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedIfAbleEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.CombatAttackService;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.service.GameRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Easy difficulty AI. Uses simple heuristics for spell casting, combat, and scoring.
 */
@Slf4j
public class EasyAiDecisionEngine extends AiDecisionEngine {

    public EasyAiDecisionEngine(UUID gameId, Player aiPlayer, GameRegistry gameRegistry,
                                MessageHandler messageHandler, GameQueryService gameQueryService,
                                CombatAttackService combatAttackService,
                                GameBroadcastService gameBroadcastService,
                                TargetValidationService targetValidationService,
                                TargetLegalityService targetLegalityService) {
        super(gameId, aiPlayer, gameRegistry, messageHandler, gameQueryService, combatAttackService, gameBroadcastService, targetValidationService, targetLegalityService);
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

        // Try casting an instant outside main phase (any priority window)
        if (tryCastInstant(gameData)) {
            return;
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
            if (card.hasType(CardType.LAND)) {
                continue;
            }
            if (card.hasType(CardType.INSTANT)) {
                continue;
            }
            if (card.getManaCost() == null) {
                continue;
            }

            if (!isSpellCastable(gameData, card, virtualPool)) {
                continue;
            }
            int score = scoreCard(gameData, card);
            if (score > 0) {
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

        // Handle modal spells (ChooseOneEffect)
        ModalCastPlan modalPlan = prepareModalSpellCast(gameData, card);
        if (modalPlan == null && findChooseOneEffect(card) != null) {
            return false;
        }

        // Build damage assignments for divided damage spells
        Map<UUID, Integer> damageAssignments = null;
        if (modalPlan == null && EffectResolution.needsDamageDistribution(card)) {
            damageAssignments = targetSelector.buildDamageAssignments(gameData, card, aiPlayer.getId());
            if (damageAssignments == null) {
                return false;
            }
        }

        // Determine target if needed (skip for modal and damage distribution spells)
        UUID targetId = modalPlan != null ? modalPlan.targetId() : null;
        List<UUID> multiTargetIds = null;
        boolean isMultiTarget = card.getSpellTargets().size() > 1;
        if (isMultiTarget && modalPlan == null) {
            multiTargetIds = targetSelector.chooseMultiTargets(gameData, card, aiPlayer.getId());
            if (multiTargetIds == null) {
                return false;
            }
        } else if (modalPlan == null && !EffectResolution.needsDamageDistribution(card) && (EffectResolution.needsTarget(card) || card.isAura())) {
            targetId = targetSelector.chooseTarget(gameData, card, aiPlayer.getId());
            if (targetId == null) {
                return false;
            }
        }

        // Check targeting tax (e.g. Kopala, Warden of Waves)
        int targetingTax = computeTargetingTax(gameData, targetId, multiTargetIds);
        if (targetingTax > 0 && !canAffordSpell(gameData, card, virtualPool, targetingTax)) {
            return false;
        }

        // Select sacrifice target if the spell has a sacrifice cost
        UUID sacrificePermanentId = selectSacrificeTarget(gameData, card);

        // Select graveyard cards to exile if the spell has a graveyard exile cost
        List<Integer> exileGraveyardCardIndices = null;
        if (findExileXGraveyardCost(card) != null) {
            exileGraveyardCardIndices = selectAllGraveyardIndices(gameData);
        } else if (findExileNGraveyardCost(card) != null) {
            exileGraveyardCardIndices = selectNGraveyardIndicesToExile(gameData, findExileNGraveyardCost(card));
        }

        // Calculate X value (for modal spells, xValue is the mode index)
        ManaCost castCost = new ManaCost(card.getManaCost());
        Integer xValue = modalPlan != null ? modalPlan.modeIndex() : null;
        int costModifier = gameBroadcastService.getCastCostModifier(gameData, aiPlayer.getId(), card) + targetingTax;
        if (castCost.hasX() && xValue == null) {
            if (hasPermanentManaValueEqualsXTarget(card)) {
                // X must match the target permanent's mana value — use max affordable X
                // (not smartX which clamps to toughness) and co-select target + X.
                int maxX = manaManager.calculateMaxAffordableX(card, virtualPool, costModifier);
                if (maxX <= 0) {
                    return false;
                }
                List<Permanent> validTargets = targetSelector.findValidPermanentTargetsForManaValueX(
                        gameData, card, aiPlayer.getId(), maxX);
                if (validTargets.isEmpty()) {
                    return false;
                }
                Permanent chosen = validTargets.stream()
                        .max(Comparator.comparingInt(p -> p.getCard().getManaValue()))
                        .orElse(validTargets.getFirst());
                targetId = chosen.getId();
                xValue = chosen.getCard().getManaValue();
            } else {
                int smartX = manaManager.calculateSmartX(gameData, card, targetId, virtualPool, costModifier);
                smartX = Math.min(smartX, getMaxXForGraveyardRequirements(gameData, card));
                if (smartX <= 0) {
                    return false;
                }
                xValue = smartX;
            }
        }

        log.info("AI: Casting {}{} in game {}", card.getName(),
                xValue != null ? " (X=" + xValue + ")" : "", gameId);
        if (tapManaForSpell(gameData, card, xValue, targetingTax)) {
            return true; // Mana ability triggered a pending choice; will resume after it resolves
        }
        final UUID finalTargetId = targetId;
        final Integer finalXValue = xValue;
        final Map<UUID, Integer> finalDamageAssignments = damageAssignments;
        final UUID finalSacrificePermanentId = sacrificePermanentId;
        final List<Integer> finalExileGraveyardCardIndices = exileGraveyardCardIndices;
        final List<UUID> finalMultiTargetIds = multiTargetIds;
        send(() -> messageHandler.handlePlayCard(selfConnection,
                new PlayCardRequest(cardIndex, finalXValue, finalTargetId, finalDamageAssignments, finalMultiTargetIds, null, null, finalSacrificePermanentId, null, null, null, null, null, finalExileGraveyardCardIndices, null, null, null)));
        // Verify the spell was actually cast — handlePlayCard silently
        // swallows errors, so we must confirm the state actually changed.
        // Identity check: hand size alone is unreliable because ETB/cast triggers
        // can add cards back to hand (e.g. Explore), masking a successful cast.
        if (hand.contains(card)) {
            ManaPool actualPool = gameData.playerManaPools.get(aiPlayer.getId());
            log.warn("AI (Easy): PlayCard failed silently in game {}. Card='{}' index={} step={} isActive={} stackEmpty={} pool={} priorityPassed={}",
                    gameId, card.getName(), cardIndex,
                    gameData.currentStep, aiPlayer.getId().equals(gameData.activePlayerId),
                    gameData.stack.isEmpty(), actualPool != null ? actualPool.toMap() : "null",
                    gameData.priorityPassedBy);
            return false;
        }
        return true;
    }

    /**
     * Tries to cast the highest-scored instant from hand. Used outside the main
     * phase so the Easy AI doesn't sit on instants forever.
     */
    private boolean tryCastInstant(GameData gameData) {
        List<Card> hand = gameData.playerHands.get(aiPlayer.getId());
        if (hand == null) {
            return false;
        }

        ManaPool virtualPool = manaManager.buildVirtualManaPool(gameData, aiPlayer.getId());

        List<int[]> castableInstants = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (!card.hasType(CardType.INSTANT)) continue;
            if (card.getManaCost() == null) continue;
            if (EffectResolution.needsSpellTarget(card)) continue; // Can't target spells on stack
            if (!isSpellCastable(gameData, card, virtualPool)) continue;
            int score = scoreCard(gameData, card);
            if (score > 0) {
                castableInstants.add(new int[]{i, score});
            }
        }

        if (castableInstants.isEmpty()) {
            return false;
        }

        castableInstants.sort((a, b) -> Integer.compare(b[1], a[1]));
        int cardIndex = castableInstants.get(0)[0];
        Card card = hand.get(cardIndex);

        // Handle modal spells (ChooseOneEffect)
        ModalCastPlan modalPlan = prepareModalSpellCast(gameData, card);
        if (modalPlan == null && findChooseOneEffect(card) != null) {
            return false;
        }

        Map<UUID, Integer> damageAssignments = null;
        if (modalPlan == null && EffectResolution.needsDamageDistribution(card)) {
            damageAssignments = targetSelector.buildDamageAssignments(gameData, card, aiPlayer.getId());
            if (damageAssignments == null) return false;
        }

        UUID targetId = modalPlan != null ? modalPlan.targetId() : null;
        List<UUID> multiTargetIds = null;
        boolean isMultiTarget = card.getSpellTargets().size() > 1;
        if (isMultiTarget && modalPlan == null) {
            multiTargetIds = targetSelector.chooseMultiTargets(gameData, card, aiPlayer.getId());
            if (multiTargetIds == null) return false;
        } else if (modalPlan == null && !EffectResolution.needsDamageDistribution(card) && (EffectResolution.needsTarget(card) || card.isAura())) {
            targetId = targetSelector.chooseTarget(gameData, card, aiPlayer.getId());
            if (targetId == null) return false;
        }

        // Check targeting tax (e.g. Kopala, Warden of Waves)
        int targetingTax = computeTargetingTax(gameData, targetId, multiTargetIds);
        if (targetingTax > 0 && !canAffordSpell(gameData, card, virtualPool, targetingTax)) {
            return false;
        }

        UUID sacrificePermanentId = selectSacrificeTarget(gameData, card);

        List<Integer> exileGraveyardCardIndices = null;
        if (findExileXGraveyardCost(card) != null) {
            exileGraveyardCardIndices = selectAllGraveyardIndices(gameData);
        } else if (findExileNGraveyardCost(card) != null) {
            exileGraveyardCardIndices = selectNGraveyardIndicesToExile(gameData, findExileNGraveyardCost(card));
        }

        ManaCost castCost = new ManaCost(card.getManaCost());
        Integer xValue = modalPlan != null ? modalPlan.modeIndex() : null;
        int instantCostModifier = gameBroadcastService.getCastCostModifier(gameData, aiPlayer.getId(), card) + targetingTax;
        if (castCost.hasX() && xValue == null) {
            if (hasPermanentManaValueEqualsXTarget(card)) {
                int maxX = manaManager.calculateMaxAffordableX(card, virtualPool, instantCostModifier);
                if (maxX <= 0) return false;
                List<Permanent> validTargets = targetSelector.findValidPermanentTargetsForManaValueX(
                        gameData, card, aiPlayer.getId(), maxX);
                if (validTargets.isEmpty()) return false;
                Permanent chosen = validTargets.stream()
                        .max(Comparator.comparingInt(p -> p.getCard().getManaValue()))
                        .orElse(validTargets.getFirst());
                targetId = chosen.getId();
                xValue = chosen.getCard().getManaValue();
            } else {
                int smartX = manaManager.calculateSmartX(gameData, card, targetId, virtualPool, instantCostModifier);
                smartX = Math.min(smartX, getMaxXForGraveyardRequirements(gameData, card));
                if (smartX <= 0) return false;
                xValue = smartX;
            }
        }

        log.info("AI: Casting instant {}{} in game {}", card.getName(),
                xValue != null ? " (X=" + xValue + ")" : "", gameId);
        if (tapManaForSpell(gameData, card, xValue, targetingTax)) {
            return true; // Mana ability triggered a pending choice; will resume after it resolves
        }
        final UUID finalTargetId = targetId;
        final Integer finalXValue = xValue;
        final Map<UUID, Integer> finalDamageAssignments = damageAssignments;
        final UUID finalSacrificePermanentId = sacrificePermanentId;
        final List<Integer> finalExileGraveyardCardIndices = exileGraveyardCardIndices;
        final List<UUID> finalMultiTargetIds = multiTargetIds;
        send(() -> messageHandler.handlePlayCard(selfConnection,
                new PlayCardRequest(cardIndex, finalXValue, finalTargetId, finalDamageAssignments, finalMultiTargetIds, null, null, finalSacrificePermanentId, null, null, null, null, null, finalExileGraveyardCardIndices, null, null, null)));
        // Identity check: hand size alone is unreliable because ETB/cast triggers
        // can add cards back to hand (e.g. Explore), masking a successful cast.
        if (hand.contains(card)) {
            log.warn("AI: Instant cast failed silently in game {}", gameId);
            return false;
        }
        return true;
    }

    protected int scoreCard(GameData gameData, Card card) {
        int score = card.getManaValue() * 10;

        if (card.hasType(CardType.CREATURE)) {
            int power = card.getPower() != null ? card.getPower() : 0;
            int toughness = card.getToughness() != null ? card.getToughness() : 0;
            score += (power + toughness) * 5;

            if (card.getKeywords().contains(Keyword.FLYING)) score += 15;
            if (card.getKeywords().contains(Keyword.FIRST_STRIKE)) score += 10;
            if (card.getKeywords().contains(Keyword.DOUBLE_STRIKE)) score += 20;
            if (card.getKeywords().contains(Keyword.TRAMPLE)) score += 10;
            if (card.getKeywords().contains(Keyword.VIGILANCE)) score += 5;
        } else if (card.hasType(CardType.ENCHANTMENT)) {
            score += 20;
        }

        // Mass damage spells are only beneficial if they kill more opponent creatures than our own
        for (var effect : card.getEffects(EffectSlot.SPELL)) {
            if (effect instanceof MassDamageEffect aoe) {
                UUID opponentId = AiUtils.getOpponentId(gameData, aiPlayer.getId());
                List<Permanent> oppField = gameData.playerBattlefields.getOrDefault(opponentId, List.of());
                List<Permanent> aiField = gameData.playerBattlefields.getOrDefault(aiPlayer.getId(), List.of());
                long oppKilled = oppField.stream()
                        .filter(p -> gameQueryService.isCreature(gameData, p)
                                && gameQueryService.getEffectiveToughness(gameData, p) <= aoe.damage())
                        .count();
                long aiKilled = aiField.stream()
                        .filter(p -> gameQueryService.isCreature(gameData, p)
                                && gameQueryService.getEffectiveToughness(gameData, p) <= aoe.damage())
                        .count();
                if (oppKilled <= aiKilled) {
                    return 0;
                }
            }
        }

        return score;
    }

    // ===== Combat =====

    @Override
    protected void handleAttackers(GameData gameData) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(aiPlayer.getId());
        List<Integer> availableIndices = combatAttackService.getAttackableCreatureIndices(gameData, aiPlayer.getId());
        if (battlefield == null || availableIndices.isEmpty()) {
            send(() -> messageHandler.handleDeclareAttackers(selfConnection, new DeclareAttackersRequest(List.of(), null)));
            return;
        }

        UUID opponentId = AiUtils.getOpponentId(gameData, aiPlayer.getId());
        List<Permanent> opponentBattlefield = gameData.playerBattlefields.getOrDefault(opponentId, List.of());

        List<Integer> attackerIndices = new ArrayList<>();
        int totalAttackDamage = 0;
        int opponentLife = gameData.getLife(opponentId);

        for (int i : availableIndices) {
            Permanent perm = battlefield.get(i);

            int power = gameQueryService.getEffectivePower(gameData, perm);
            int toughness = gameQueryService.getEffectiveToughness(gameData, perm);

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
            for (int i : availableIndices) {
                allInDamage += gameQueryService.getEffectivePower(gameData, battlefield.get(i));
            }
            if (allInDamage >= opponentLife) {
                attackerIndices = new ArrayList<>(availableIndices);
            }
        }

        // Ensure creatures with "attacks each combat if able" are included
        List<Integer> mustAttackIndices = combatAttackService.getMustAttackIndices(gameData, aiPlayer.getId(), availableIndices);
        attackerIndices = enforceMustAttack(attackerIndices, mustAttackIndices);

        // Ensure at least one attacker when forced (e.g. Trove of Temptation)
        attackerIndices = enforceMustAttackWithAtLeastOne(gameData, attackerIndices, availableIndices);

        // Cap attackers to what we can afford given attack tax, and tap mana to pay
        attackerIndices = prepareAttackersForTax(gameData, attackerIndices);

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

        // Collect attackers (skip unblockable ones) and sort by priority:
        //   lure > menace-lure > mustBlockIfAble > regular (power desc).
        // Lure goes first because it forces all able blockers; within lure, menace jumps ahead
        // so a menace+lure attacker claims a legal 2+ pool before a non-menace lure drains it.
        List<int[]> attackers = new ArrayList<>();
        for (int i = 0; i < opponentBattlefield.size(); i++) {
            Permanent perm = opponentBattlefield.get(i);
            if (!perm.isAttacking()) continue;
            if (gameQueryService.hasCantBeBlocked(gameData, perm)) continue;
            attackers.add(new int[]{i,
                    gameQueryService.getEffectivePower(gameData, perm),
                    gameQueryService.getEffectiveToughness(gameData, perm)});
        }

        attackers.sort((a, b) -> {
            Permanent pa = opponentBattlefield.get(a[0]);
            Permanent pb = opponentBattlefield.get(b[0]);
            int lureCmp = Boolean.compare(hasLureEffect(gameData, pb), hasLureEffect(gameData, pa));
            if (lureCmp != 0) return lureCmp;
            int menaceCmp = Boolean.compare(
                    gameQueryService.hasKeyword(gameData, pb, Keyword.MENACE),
                    gameQueryService.hasKeyword(gameData, pa, Keyword.MENACE));
            if (menaceCmp != 0) return menaceCmp;
            int mbCmp = Boolean.compare(hasMustBeBlockedIfAble(gameData, pb),
                                        hasMustBeBlockedIfAble(gameData, pa));
            if (mbCmp != 0) return mbCmp;
            return Integer.compare(b[1], a[1]);
        });

        List<BlockerAssignment> assignments = new ArrayList<>();
        boolean[] blockerUsed = new boolean[battlefield.size()];

        int totalIncomingDamage = attackers.stream().mapToInt(a -> a[1]).sum();
        int myLife = gameData.getLife(aiPlayer.getId());

        for (int[] attacker : attackers) {
            int attackerIdx = attacker[0];
            int attackerPower = attacker[1];
            int attackerToughness = attacker[2];
            Permanent attackingPerm = opponentBattlefield.get(attackerIdx);
            boolean menace = gameQueryService.hasKeyword(gameData, attackingPerm, Keyword.MENACE);
            boolean lure = hasLureEffect(gameData, attackingPerm);
            boolean mustBlock = hasMustBeBlockedIfAble(gameData, attackingPerm);
            boolean lethalIncoming = totalIncomingDamage >= myLife;

            List<Integer> candidates = getAvailableBlockersForAttacker(gameData, battlefield, blockerUsed, attackingPerm);
            if (candidates.isEmpty()) continue;
            // Menace: no creature is "able to block" alone — with <2 candidates, skip.
            if (menace && candidates.size() < 2) continue;

            List<Integer> chosen;
            if (lure) {
                // Every able blocker must block this attacker.
                chosen = new ArrayList<>(candidates);
            } else if (menace) {
                // Menace attacker: need a favorable pair, or chump pair if lethal, or force pair if mandatory.
                List<Integer> favorablePair = selectBestFavorablePair(gameData, battlefield, candidates, attackerPower, attackerToughness);
                if (favorablePair != null) {
                    chosen = favorablePair;
                } else if (lethalIncoming || mustBlock) {
                    chosen = pickCheapestBlockers(battlefield, candidates, 2);
                } else {
                    chosen = List.of();
                }
            } else {
                // Non-menace regular attacker.
                int killAndSurviveIdx = findCheapestKillingBlocker(gameData, battlefield, candidates, attackerPower, attackerToughness);
                List<Integer> favorablePair = selectBestFavorablePair(gameData, battlefield, candidates, attackerPower, attackerToughness);
                if (killAndSurviveIdx != -1) {
                    chosen = List.of(killAndSurviveIdx);
                } else if (favorablePair != null) {
                    chosen = favorablePair;
                } else if (lethalIncoming || mustBlock) {
                    chosen = pickCheapestBlockers(battlefield, candidates, 1);
                } else {
                    chosen = List.of();
                }
            }

            if (chosen.isEmpty()) continue;

            for (int blockerIdx : chosen) {
                assignments.add(new BlockerAssignment(blockerIdx, attackerIdx));
                blockerUsed[blockerIdx] = true;
            }
            totalIncomingDamage -= attackerPower;
        }

        log.info("AI: Declaring {} blockers in game {}", assignments.size(), gameId);
        sendBlockerDeclaration(gameData, new DeclareBlockersRequest(assignments));
    }

    private int findCheapestKillingBlocker(GameData gameData, List<Permanent> battlefield,
                                           List<Integer> availableBlockers,
                                           int attackerPower, int attackerToughness) {
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
        return bestBlockerIdx;
    }

    private boolean hasLureEffect(GameData gameData, Permanent attacker) {
        boolean hasOnCard = attacker.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(MustBeBlockedByAllCreaturesEffect.class::isInstance);
        if (hasOnCard) return true;
        return gameQueryService.hasAuraWithEffect(gameData, attacker, MustBeBlockedByAllCreaturesEffect.class);
    }

    private boolean hasMustBeBlockedIfAble(GameData gameData, Permanent attacker) {
        if (attacker.isMustBeBlockedThisTurn()) return true;
        boolean hasOnCard = attacker.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(MustBeBlockedIfAbleEffect.class::isInstance);
        if (hasOnCard) return true;
        return gameQueryService.hasAuraWithEffect(gameData, attacker, MustBeBlockedIfAbleEffect.class);
    }

    // ===== Combat Helpers =====

    private Permanent findBestBlocker(GameData gameData, Permanent attacker, List<Permanent> opponentField) {
        Permanent best = null;
        int bestToughness = Integer.MAX_VALUE;

        for (Permanent opp : opponentField) {
            if (!gameQueryService.canBlock(gameData, opp)) continue;
            if (!gameQueryService.canBlockAttacker(gameData, opp, attacker, opponentField)) continue;

            int oppToughness = gameQueryService.getEffectiveToughness(gameData, opp);
            if (best == null || oppToughness < bestToughness) {
                best = opp;
                bestToughness = oppToughness;
            }
        }
        return best;
    }

    private List<Integer> getAvailableBlockersForAttacker(GameData gameData, List<Permanent> battlefield, boolean[] blockerUsed,
                                                          Permanent attackingPerm) {
        List<Permanent> defenderBattlefield = battlefield;
        List<Integer> available = new ArrayList<>();
        for (int j = 0; j < battlefield.size(); j++) {
            if (blockerUsed[j]) continue;
            Permanent blocker = battlefield.get(j);
            if (!gameQueryService.canBlock(gameData, blocker)) continue;
            if (!gameQueryService.canBlockAttacker(gameData, blocker, attackingPerm, defenderBattlefield)) continue;
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
