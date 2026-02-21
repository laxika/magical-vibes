package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ColorChoiceContext;
import com.github.laxika.magicalvibes.model.DrawReplacementKind;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.InteractionContext;
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
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect.Scope;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.networking.Connection;
import com.github.laxika.magicalvibes.networking.MessageHandler;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.networking.message.BottomCardsRequest;
import com.github.laxika.magicalvibes.networking.message.CombatDamageAssignedRequest;
import com.github.laxika.magicalvibes.networking.message.CardChosenRequest;
import com.github.laxika.magicalvibes.networking.message.ColorChosenRequest;
import com.github.laxika.magicalvibes.networking.message.DeclareAttackersRequest;
import com.github.laxika.magicalvibes.networking.message.DeclareBlockersRequest;
import com.github.laxika.magicalvibes.networking.message.GraveyardCardChosenRequest;
import com.github.laxika.magicalvibes.networking.message.HandTopBottomChosenRequest;
import com.github.laxika.magicalvibes.networking.message.KeepHandRequest;
import com.github.laxika.magicalvibes.networking.message.LibraryCardChosenRequest;
import com.github.laxika.magicalvibes.networking.message.MayAbilityChosenRequest;
import com.github.laxika.magicalvibes.networking.message.MulliganRequest;
import com.github.laxika.magicalvibes.networking.message.MultipleGraveyardCardsChosenRequest;
import com.github.laxika.magicalvibes.networking.message.MultiplePermanentsChosenRequest;
import com.github.laxika.magicalvibes.networking.message.PassPriorityRequest;
import com.github.laxika.magicalvibes.networking.message.PermanentChosenRequest;
import com.github.laxika.magicalvibes.networking.message.PlayCardRequest;
import com.github.laxika.magicalvibes.networking.message.ReorderLibraryCardsRequest;
import com.github.laxika.magicalvibes.networking.message.TapPermanentRequest;
import com.github.laxika.magicalvibes.service.GameQueryService;
import com.github.laxika.magicalvibes.service.GameRegistry;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
public class AiDecisionEngine {

    private final UUID gameId;
    private final Player aiPlayer;
    private final GameRegistry gameRegistry;
    private final MessageHandler messageHandler;
    private final GameQueryService gameQueryService;

    @Setter
    private Connection selfConnection;

    public AiDecisionEngine(UUID gameId, Player aiPlayer, GameRegistry gameRegistry,
                            MessageHandler messageHandler, GameQueryService gameQueryService) {
        this.gameId = gameId;
        this.aiPlayer = aiPlayer;
        this.gameRegistry = gameRegistry;
        this.messageHandler = messageHandler;
        this.gameQueryService = gameQueryService;
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
            case "COMBAT_DAMAGE_ASSIGNMENT" -> handleCombatDamageAssignment(gameData);
            case "GAME_OVER" -> log.info("AI: Game {} is over", gameId);
            default -> {
                // Ignore informational messages (BATTLEFIELD_UPDATED, MANA_UPDATED, etc.)
            }
        }
    }

    // ===== Mulligan =====

    public void handleInitialMulligan() {
        GameData gameData = gameRegistry.get(gameId);
        if (gameData == null) return;
        if (shouldKeepHand(gameData)) {
            log.info("AI: Keeping hand in game {}", gameId);
            send(() -> messageHandler.handleKeepHand(selfConnection, new KeepHandRequest()));
        } else {
            log.info("AI: Taking mulligan in game {}", gameId);
            send(() -> messageHandler.handleMulligan(selfConnection, new MulliganRequest()));
        }
    }

    private void handleMulliganResolved(GameData gameData) {
        if (gameData.playerKeptHand.contains(aiPlayer.getId())) {
            return;
        }
        handleInitialMulligan();
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
        send(() -> messageHandler.handleBottomCards(selfConnection, new BottomCardsRequest(toBottom)));
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
        if (gameData.interaction.isAwaitingInput()) {
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
        send(() -> messageHandler.handlePassPriority(selfConnection, new PassPriorityRequest()));
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
                final int idx = i;
                send(() -> messageHandler.handlePlayCard(selfConnection,
                        new PlayCardRequest(idx, null, null, null, null, null, null)));
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
        final UUID finalTargetId = targetId;
        send(() -> messageHandler.handlePlayCard(selfConnection,
                new PlayCardRequest(cardIndex, null, finalTargetId, null, null, null, null)));
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
                if (gameQueryService.isCreature(gameData, perm) && perm.isSummoningSick()
                        && !gameQueryService.hasKeyword(gameData, perm, Keyword.HASTE)) {
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
            if (gameQueryService.isCreature(gameData, perm) && perm.isSummoningSick()
                    && !gameQueryService.hasKeyword(gameData, perm, Keyword.HASTE)) {
                continue;
            }

            boolean producesMana = perm.getCard().getEffects(EffectSlot.ON_TAP).stream()
                    .anyMatch(e -> e instanceof AwardManaEffect || e instanceof AwardAnyColorManaEffect);
            if (!producesMana) {
                continue;
            }

            final int idx = i;
            send(() -> messageHandler.handleTapPermanent(selfConnection, new TapPermanentRequest(idx)));

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

        // Handle ETB destroy effects (e.g., Aven Cloudchaser targets enchantments, Nekrataal targets creatures)
        for (CardEffect effect : card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)) {
            if (effect instanceof DestroyTargetPermanentEffect) {
                return chooseDestroyTarget(gameData, card, opponentId);
            }
        }

        boolean isBeneficial = false;
        if (card.isAura()) {
            for (CardEffect effect : card.getEffects(EffectSlot.STATIC)) {
                if (effect instanceof BoostEnchantedCreatureEffect
                        || (effect instanceof GrantKeywordEffect grant && grant.scope() == Scope.ENCHANTED_CREATURE)) {
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
                        .filter(p -> gameQueryService.isCreature(gameData, p))
                        .max(Comparator.comparingInt(p -> gameQueryService.getEffectiveToughness(gameData, p)))
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
                        .filter(p -> gameQueryService.isCreature(gameData, p))
                        .filter(p -> auraEffectClasses.stream().noneMatch(ec -> gameQueryService.hasAuraWithEffect(gameData, p, ec)))
                        .max(Comparator.comparingInt(p -> gameQueryService.getEffectivePower(gameData, p)))
                        .map(Permanent::getId)
                        .orElse(null);
            }
        }
        return null;
    }

    private UUID chooseDestroyTarget(GameData gameData, Card card, UUID opponentId) {
        // Search opponent's battlefield first (prefer destroying opponent's permanents)
        List<Permanent> oppBattlefield = gameData.playerBattlefields.getOrDefault(opponentId, List.of());
        UUID oppTarget = findDestroyCandidate(gameData, card, oppBattlefield);
        if (oppTarget != null) {
            return oppTarget;
        }

        // Fall back to own battlefield (e.g., destroying a negative enchantment on own creature)
        List<Permanent> ownBattlefield = gameData.playerBattlefields.getOrDefault(aiPlayer.getId(), List.of());
        return findDestroyCandidate(gameData, card, ownBattlefield);
    }

    private UUID findDestroyCandidate(GameData gameData, Card card, List<Permanent> battlefield) {
        List<Permanent> candidates = battlefield.stream()
                .filter(p -> card.getTargetFilter() == null || passesTargetFilter(card, p))
                .toList();

        if (candidates.isEmpty()) {
            return null;
        }

        // Prefer creature kills when legal, then choose the most threatening one.
        UUID creatureTarget = candidates.stream()
                .filter(p -> gameQueryService.isCreature(gameData, p))
                .max(Comparator.comparingInt(p -> gameQueryService.getEffectivePower(gameData, p)))
                .map(Permanent::getId)
                .orElse(null);
        if (creatureTarget != null) {
            return creatureTarget;
        }

        return candidates.getFirst().getId();
    }

    private boolean passesTargetFilter(Card card, Permanent target) {
        if (card.getTargetFilter() == null) {
            return true;
        }
        try {
            gameQueryService.validateTargetFilter(card.getTargetFilter(),
                    target,
                    FilterContext.of(gameRegistry.get(gameId))
                            .withSourceCardId(card.getId())
                            .withSourceControllerId(aiPlayer.getId()));
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    // ===== Combat =====

    private void handleAttackers(GameData gameData) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(aiPlayer.getId());
        if (battlefield == null) {
            send(() -> messageHandler.handleDeclareAttackers(selfConnection, new DeclareAttackersRequest(List.of())));
            return;
        }

        UUID opponentId = getOpponentId(gameData);
        List<Permanent> opponentBattlefield = gameData.playerBattlefields.getOrDefault(opponentId, List.of());

        List<Integer> attackerIndices = new ArrayList<>();
        int totalAttackDamage = 0;
        int opponentLife = gameData.playerLifeTotals.getOrDefault(opponentId, 20);

        for (int i = 0; i < battlefield.size(); i++) {
            Permanent perm = battlefield.get(i);
            if (!gameQueryService.isCreature(gameData, perm)) continue;
            if (perm.isTapped()) continue;
            if (perm.isSummoningSick() && !gameQueryService.hasKeyword(gameData, perm, Keyword.HASTE)) continue;
            if (gameQueryService.hasKeyword(gameData, perm, Keyword.DEFENDER)) continue;

            int power = gameQueryService.getEffectivePower(gameData, perm);
            int toughness = gameQueryService.getEffectiveToughness(gameData, perm);
            boolean hasFlying = gameQueryService.hasKeyword(gameData, perm, Keyword.FLYING);

            // Find the best potential blocker
            Permanent bestBlocker = findBestBlocker(gameData, perm, opponentBattlefield);

            boolean shouldAttack = false;
            if (bestBlocker == null) {
                // No blocker available — safe to attack
                shouldAttack = true;
            } else {
                int blockerPower = gameQueryService.getEffectivePower(gameData, bestBlocker);
                int blockerToughness = gameQueryService.getEffectiveToughness(gameData, bestBlocker);

                // Attack if we kill the blocker and survive
                if (power >= blockerToughness && blockerPower < toughness) {
                    shouldAttack = true;
                }
                // Attack if favorable mana-value trade (we kill them, even if we die too)
                else if (power >= blockerToughness && perm.getCard().getManaValue() < bestBlocker.getCard().getManaValue()) {
                    shouldAttack = true;
                }
                // Attack with evasion creatures (flying that can't be blocked by reach-less ground)
                else if (hasFlying && !gameQueryService.hasKeyword(gameData, bestBlocker, Keyword.FLYING)
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
            // Check if all-in attack is lethal
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
        send(() -> messageHandler.handleDeclareAttackers(selfConnection, new DeclareAttackersRequest(finalAttackerIndices)));
    }

    private Permanent findBestBlocker(GameData gameData, Permanent attacker, List<Permanent> opponentField) {
        boolean hasFlying = gameQueryService.hasKeyword(gameData, attacker, Keyword.FLYING);

        Permanent best = null;
        int bestToughness = Integer.MAX_VALUE;

        for (Permanent opp : opponentField) {
            if (!gameQueryService.isCreature(gameData, opp)) continue;
            if (opp.isTapped()) continue;
            if (gameQueryService.hasKeyword(gameData, opp, Keyword.DEFENDER) || !opp.isSummoningSick() || true) {
                // Can potentially block
            }

            // Flying creatures can only be blocked by flying or reach
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

    private void handleBlockers(GameData gameData) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(aiPlayer.getId());
        UUID opponentId = getOpponentId(gameData);
        List<Permanent> opponentBattlefield = gameData.playerBattlefields.getOrDefault(opponentId, List.of());

        if (battlefield == null || opponentBattlefield == null) {
            send(() -> messageHandler.handleDeclareBlockers(selfConnection, new DeclareBlockersRequest(List.of())));
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
                        gameQueryService.getEffectivePower(gameData, perm),
                        gameQueryService.getEffectiveToughness(gameData, perm)});
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

                // Favorable block: we kill attacker and survive
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
                // Chump block to survive
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

    // ===== Choice Handlers =====

    private void handleCardChoice(GameData gameData) {
        InteractionContext.CardChoice cardChoice = gameData.interaction.cardChoiceContext();
        if (cardChoice == null) {
            return;
        }
        UUID choicePlayerId = cardChoice.playerId();
        Set<Integer> validIndices = cardChoice.validIndices();

        // Discard: pick highest mana cost card
        if (!aiPlayer.getId().equals(choicePlayerId)) {
            return;
        }

        List<Card> hand = gameData.playerHands.get(aiPlayer.getId());
        if (hand == null || validIndices == null || validIndices.isEmpty()) {
            return;
        }

        int bestIndex = validIndices.stream()
                .max(Comparator.comparingInt(i -> hand.get(i).getManaValue()))
                .orElse(validIndices.iterator().next());

        log.info("AI: Choosing card at index {} in game {}", bestIndex, gameId);
        send(() -> messageHandler.handleCardChosen(selfConnection, new CardChosenRequest(bestIndex)));
    }

    private void handlePermanentChoice(GameData gameData) {
        InteractionContext.PermanentChoice permanentChoice = gameData.interaction.permanentChoiceContextView();
        if (permanentChoice == null) {
            return;
        }
        UUID choicePlayerId = permanentChoice.playerId();
        Set<UUID> validIds = permanentChoice.validIds();

        if (!aiPlayer.getId().equals(choicePlayerId)) {
            return;
        }

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
                .filter(p -> gameQueryService.isCreature(gameData, p))
                .max(Comparator.comparingInt(p -> gameQueryService.getEffectivePower(gameData, p)))
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
        final UUID finalBest = best;
        send(() -> messageHandler.handlePermanentChosen(selfConnection, new PermanentChosenRequest(finalBest)));
    }

    private void handleMultiPermanentChoice(GameData gameData) {
        InteractionContext.MultiPermanentChoice multiPermanentChoice = gameData.interaction.multiPermanentChoiceContext();
        if (multiPermanentChoice == null) {
            return;
        }
        UUID choicePlayerId = multiPermanentChoice.playerId();
        Set<UUID> validIds = multiPermanentChoice.validIds();
        int maxCount = multiPermanentChoice.maxCount();

        if (!aiPlayer.getId().equals(choicePlayerId)) {
            return;
        }

        if (validIds == null || validIds.isEmpty()) {
            return;
        }

        UUID opponentId = getOpponentId(gameData);
        List<Permanent> opponentField = gameData.playerBattlefields.getOrDefault(opponentId, List.of());

        // Pick opponent's biggest threats
        List<UUID> chosen = opponentField.stream()
                .filter(p -> validIds.contains(p.getId()))
                .sorted(Comparator.comparingInt((Permanent p) -> gameQueryService.getEffectivePower(gameData, p)).reversed())
                .limit(maxCount)
                .map(Permanent::getId)
                .toList();

        if (chosen.isEmpty()) {
            // Fall back to any valid
            chosen = validIds.stream().limit(maxCount).toList();
        }

        log.info("AI: Choosing {} permanents in game {}", chosen.size(), gameId);
        final List<UUID> finalChosen = chosen;
        send(() -> messageHandler.handleMultiplePermanentsChosen(selfConnection, new MultiplePermanentsChosenRequest(finalChosen)));
    }

    private void handleColorChoice(GameData gameData) {
        InteractionContext.ColorChoice colorChoice = gameData.interaction.colorChoiceContextView();
        if (colorChoice == null) {
            return;
        }
        UUID choicePlayerId = colorChoice.playerId();

        if (!aiPlayer.getId().equals(choicePlayerId)) {
            return;
        }

        if (colorChoice.context() instanceof ColorChoiceContext.DrawReplacementChoice drc
                && drc.kind() == DrawReplacementKind.ABUNDANCE) {
            log.info("AI: Choosing NONLAND for Abundance in game {}", gameId);
            send(() -> messageHandler.handleColorChosen(selfConnection, new ColorChosenRequest(null, "NONLAND")));
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
        final String colorName = bestColor.name();
        send(() -> messageHandler.handleColorChosen(selfConnection, new ColorChosenRequest(null, colorName)));
    }

    private void handleMayAbilityChoice(GameData gameData) {
        InteractionContext.MayAbilityChoice mayAbilityChoice = gameData.interaction.mayAbilityChoiceContext();
        if (mayAbilityChoice == null) {
            return;
        }
        UUID choicePlayerId = mayAbilityChoice.playerId();

        if (!aiPlayer.getId().equals(choicePlayerId)) {
            return;
        }

        // Generally accept may abilities
        log.info("AI: Accepting may ability in game {}", gameId);
        send(() -> messageHandler.handleMayAbilityChosen(selfConnection, new MayAbilityChosenRequest(null, true)));
    }

    private void handleReorderCards(GameData gameData) {
        InteractionContext.LibraryReorder libraryReorder = gameData.interaction.libraryReorderContext();
        if (libraryReorder == null) {
            return;
        }
        UUID choicePlayerId = libraryReorder.playerId();
        List<Card> cards = libraryReorder.cards();

        if (!aiPlayer.getId().equals(choicePlayerId)) {
            return;
        }

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
        send(() -> messageHandler.handleLibraryCardsReordered(selfConnection, new ReorderLibraryCardsRequest(order)));
    }

    private void handleLibrarySearch(GameData gameData) {
        InteractionContext.LibrarySearch librarySearch = gameData.interaction.librarySearchContext();
        if (librarySearch == null) {
            return;
        }
        UUID choicePlayerId = librarySearch.playerId();
        List<Card> searchCards = librarySearch.cards();

        if (!aiPlayer.getId().equals(choicePlayerId)) {
            return;
        }

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
        final int idx = bestIndex;
        send(() -> messageHandler.handleLibraryCardChosen(selfConnection, new LibraryCardChosenRequest(idx)));
    }

    private void handleGraveyardChoice(GameData gameData) {
        InteractionContext.GraveyardChoice graveyardChoice = gameData.interaction.graveyardChoiceContext();
        if (graveyardChoice == null) {
            return;
        }
        UUID choicePlayerId = graveyardChoice.playerId();
        Set<Integer> validIndices = graveyardChoice.validIndices();

        if (!aiPlayer.getId().equals(choicePlayerId)) {
            return;
        }

        if (validIndices == null || validIndices.isEmpty()) {
            return;
        }

        // Pick highest mana value card
        List<Card> graveyard = graveyardChoice.cardPool();
        if (graveyard == null) {
            graveyard = gameData.playerGraveyards.getOrDefault(aiPlayer.getId(), List.of());
        }

        final List<Card> gy = graveyard;
        int bestIndex = validIndices.stream()
                .max(Comparator.comparingInt(i -> i < gy.size() ? gy.get(i).getManaValue() : 0))
                .orElse(validIndices.iterator().next());

        log.info("AI: Choosing graveyard card at index {} in game {}", bestIndex, gameId);
        send(() -> messageHandler.handleGraveyardCardChosen(selfConnection, new GraveyardCardChosenRequest(bestIndex)));
    }

    private void handleMultiGraveyardChoice(GameData gameData) {
        InteractionContext.MultiGraveyardChoice multiGraveyardChoice = gameData.interaction.multiGraveyardChoiceContext();
        if (multiGraveyardChoice == null) {
            return;
        }
        UUID choicePlayerId = multiGraveyardChoice.playerId();
        Set<UUID> validIds = multiGraveyardChoice.validCardIds();
        int maxCount = multiGraveyardChoice.maxCount();

        if (!aiPlayer.getId().equals(choicePlayerId)) {
            return;
        }

        if (validIds == null || validIds.isEmpty()) {
            return;
        }

        List<UUID> chosen = validIds.stream().limit(maxCount).toList();

        log.info("AI: Choosing {} graveyard cards in game {}", chosen.size(), gameId);
        send(() -> messageHandler.handleMultipleGraveyardCardsChosen(selfConnection, new MultipleGraveyardCardsChosenRequest(chosen)));
    }

    private void handleHandTopBottom(GameData gameData) {
        InteractionContext.HandTopBottomChoice handTopBottomChoice = gameData.interaction.handTopBottomChoiceContext();
        if (handTopBottomChoice == null) {
            return;
        }
        UUID choicePlayerId = handTopBottomChoice.playerId();
        List<Card> cards = handTopBottomChoice.cards();

        if (!aiPlayer.getId().equals(choicePlayerId)) {
            return;
        }

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
        final int h = handCardIndex;
        final int t = topCardIndex;
        send(() -> messageHandler.handleHandTopBottomChosen(selfConnection, new HandTopBottomChosenRequest(h, t)));
    }

    private void handleRevealedHandChoice(GameData gameData) {
        InteractionContext.RevealedHandChoice revealedHandChoice = gameData.interaction.revealedHandChoiceContext();
        if (revealedHandChoice == null) {
            return;
        }
        UUID choosingPlayerId = revealedHandChoice.choosingPlayerId();

        // We're choosing a card from the opponent's revealed hand
        if (!aiPlayer.getId().equals(choosingPlayerId)) {
            return;
        }

        UUID targetPlayerId = revealedHandChoice.targetPlayerId();
        if (targetPlayerId == null) {
            return;
        }

        List<Card> targetHand = gameData.playerHands.get(targetPlayerId);
        Set<Integer> validIndices = revealedHandChoice.validIndices();
        if (targetHand == null || validIndices == null || validIndices.isEmpty()) {
            return;
        }

        // Pick highest mana value card from opponent's hand
        int bestIndex = validIndices.stream()
                .max(Comparator.comparingInt(i -> targetHand.get(i).getManaValue()))
                .orElse(validIndices.iterator().next());

        log.info("AI: Choosing card {} from revealed hand in game {}", bestIndex, gameId);
        send(() -> messageHandler.handleCardChosen(selfConnection, new CardChosenRequest(bestIndex)));
    }

    // ===== Combat damage assignment =====

    private void handleCombatDamageAssignment(GameData gameData) {
        InteractionContext.CombatDamageAssignment cda = gameData.interaction.combatDamageAssignmentContext();
        if (cda == null || !aiPlayer.getId().equals(cda.playerId())) {
            return;
        }

        int atkIdx = cda.attackerIndex();
        int totalDamage = cda.totalDamage();
        var targets = cda.validTargets();
        boolean isTrample = cda.isTrample();

        // AI auto-assigns: lethal to each blocker in order, excess to player if trample/unblocked
        Map<String, Integer> assignments = new HashMap<>();
        int remaining = totalDamage;

        for (var target : targets) {
            if (target.isPlayer()) continue;
            int lethal = target.effectiveToughness() - target.currentDamage();
            int dmg = Math.min(remaining, lethal);
            if (dmg > 0) {
                assignments.put(target.id().toString(), dmg);
                remaining -= dmg;
            }
        }

        // Assign remaining to defending player if available
        if (remaining > 0) {
            for (var target : targets) {
                if (target.isPlayer()) {
                    assignments.put(target.id().toString(), remaining);
                    remaining = 0;
                    break;
                }
            }
        }

        // If no player target and remaining damage, dump on first blocker
        if (remaining > 0 && !targets.isEmpty()) {
            var firstBlocker = targets.stream().filter(t -> !t.isPlayer()).findFirst().orElse(targets.get(0));
            assignments.merge(firstBlocker.id().toString(), remaining, Integer::sum);
        }

        log.info("AI: Assigning combat damage for attacker {} in game {}: {}", atkIdx, gameId, assignments);
        send(() -> messageHandler.handleCombatDamageAssigned(selfConnection,
                new CombatDamageAssignedRequest(atkIdx, assignments)));
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

    private void send(MessageHandlerAction action) {
        try {
            action.execute();
        } catch (Exception e) {
            log.error("AI: Error sending message in game {}", gameId, e);
        }
    }

    @FunctionalInterface
    private interface MessageHandlerAction {
        void execute() throws Exception;
    }
}


