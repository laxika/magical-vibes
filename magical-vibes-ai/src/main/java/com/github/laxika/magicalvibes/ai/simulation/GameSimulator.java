package com.github.laxika.magicalvibes.ai.simulation;

import com.github.laxika.magicalvibes.ai.AiManaManager;
import com.github.laxika.magicalvibes.ai.BoardEvaluator;
import com.github.laxika.magicalvibes.ai.CombatSimulator;
import com.github.laxika.magicalvibes.ai.SpellEvaluator;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TargetType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorChosenSubtypeCreatureManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.combat.CombatAttackService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.GameService;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Provides MCTS simulation capabilities using the headless Spring engine context.
 *
 * Key responsibilities:
 * - Enumerate legal actions for a given game state and player
 * - Apply actions to a (copied) game state
 * - Auto-resolve opponent decisions using heuristics
 * - Detect terminal states
 */
@Slf4j
public class GameSimulator {

    private final GameService gameService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;
    private final com.github.laxika.magicalvibes.service.cast.CastingCostService castingCostService;
    private final AiManaManager manaManager;
    private final GameRegistry gameRegistry;
    private final BoardEvaluator boardEvaluator;
    private final SpellEvaluator spellEvaluator;
    private final CombatSimulator combatSimulator;
    private final CombatAttackService combatAttackService;

    /**
     * Returns the cached headless simulator. {@link GameQueryService} instances from the same
     * {@link com.github.laxika.magicalvibes.service.GameEngineConfig} graph are behaviorally
     * identical, so the live game's bean and the headless context bean are interchangeable for MCTS.
     */
    public static GameSimulator forQueryService(GameQueryService sharedQueryService) {
        return HeadlessSimulationContext.getSimulator();
    }

    GameSimulator(GameService gameService,
                  GameQueryService gameQueryService,
                  GameBroadcastService gameBroadcastService,
                  com.github.laxika.magicalvibes.service.cast.CastingCostService castingCostService,
                  GameRegistry gameRegistry,
                  CombatAttackService combatAttackService) {
        this.gameService = gameService;
        this.gameQueryService = gameQueryService;
        this.predicateEvaluationService = new PredicateEvaluationService(gameQueryService);
        this.gameBroadcastService = gameBroadcastService;
        this.castingCostService = castingCostService;
        this.gameRegistry = gameRegistry;
        this.combatAttackService = combatAttackService;
        this.manaManager = new AiManaManager(gameQueryService);
        this.boardEvaluator = new BoardEvaluator(gameQueryService);
        this.spellEvaluator = new SpellEvaluator(gameQueryService, boardEvaluator);
        this.combatSimulator = new CombatSimulator(gameQueryService, boardEvaluator);
    }


    /**
     * Returns the list of legal actions for the given player in the current game state.
     */
    public List<SimulationAction> getLegalActions(GameData gd, UUID playerId) {
        List<SimulationAction> actions = new ArrayList<>();

        PendingInteraction awaitingInput = gd.interaction.activeInteraction();

        if (awaitingInput == null) {
            // Normal priority — can cast spells or pass
            boolean isMainPhase = gd.currentStep == TurnStep.PRECOMBAT_MAIN
                    || gd.currentStep == TurnStep.POSTCOMBAT_MAIN;
            boolean isActivePlayer = playerId.equals(gd.activePlayerId);

            if (isMainPhase && isActivePlayer && gd.stack.isEmpty()) {
                // Enumerate castable spells
                List<Card> hand = gd.playerHands.get(playerId);
                if (hand != null) {
                    ManaPool virtualPool = manaManager.buildVirtualManaPool(gd, playerId);
                    for (int i = 0; i < hand.size(); i++) {
                        Card card = hand.get(i);
                        // Policy (which legal moves to try): rollouts don't sequence land drops
                        // here and only cast at sorcery speed, so lands and instants are skipped.
                        if (card.hasType(CardType.LAND)) continue;
                        if (card.hasType(CardType.INSTANT)) continue;
                        // The simulator's downstream mana-payment / X helpers dereference the mana
                        // cost, so costless alternate-cast cards are out of its scope.
                        if (card.getManaCost() == null) continue;
                        boolean hasX = new ManaCost(card.getManaCost()).hasX();
                        // Legality/affordability is the engine's call: isCardPlayable covers mana
                        // (every cost modifier / alternative-cost route, requiresCreatureMana),
                        // timing, spell limits, target availability, ExileN and legendary-sorcery
                        // rules. X>=1 stays AI policy — passed as the extra generic requirement,
                        // mirroring AiDecisionEngine.canAffordSpell.
                        int minXPolicy = hasX ? 1 : 0;
                        if (!gameBroadcastService.isCardPlayable(gd, playerId, card, virtualPool, minXPolicy)) {
                            continue;
                        }
                        // Non-mana additional costs (sacrifice / graveyard-exile) must be payable.
                        if (!castingCostService.canPayAdditionalSpellCosts(gd, playerId, card)) {
                            continue;
                        }
                        // For targeted spells, try to find a target (policy: which target to try)
                        UUID targetId = null;
                        if (EffectResolution.needsTarget(card) || card.isAura()) {
                            targetId = findBestTarget(gd, card, playerId);
                            if (targetId == null) continue; // no valid target
                        }
                        int xValue = 0;
                        if (hasX) {
                            xValue = calculateSmartX(gd, card, targetId, virtualPool);
                            if (xValue <= 0) continue;
                        }
                        actions.add(new SimulationAction.PlayCard(i, targetId, xValue));
                    }
                }
            }
            // Always can pass priority
            actions.add(new SimulationAction.PassPriority());
            return actions;
        }

        switch (awaitingInput) {
            case PendingInteraction.AttackerDeclaration ignored -> {
                List<Integer> availableIndices = combatAttackService.getAttackableCreatureIndices(gd, playerId);
                List<Integer> mustAttackIndices = combatAttackService.getMustAttackIndices(gd, playerId, availableIndices);
                // Use CombatSimulator to find best attackers, then also offer empty/must-only attack
                List<Integer> bestAttackers = combatSimulator.findBestAttackers(gd, playerId, availableIndices, mustAttackIndices);
                boolean forcedToAttack = combatAttackService.isOpponentForcedToAttack(gd, playerId);
                if (mustAttackIndices.isEmpty() && !forcedToAttack) {
                    actions.add(new SimulationAction.DeclareAttackers(List.of())); // no attack
                } else if (mustAttackIndices.isEmpty() && forcedToAttack) {
                    // Forced to attack with at least one — offer the first available
                    actions.add(new SimulationAction.DeclareAttackers(List.of(availableIndices.getFirst())));
                } else {
                    // Must-attack creatures must always be included
                    actions.add(new SimulationAction.DeclareAttackers(mustAttackIndices));
                }
                if (!bestAttackers.isEmpty() && !bestAttackers.equals(mustAttackIndices)) {
                    actions.add(new SimulationAction.DeclareAttackers(bestAttackers));
                }
                // Also try all-in attack if different from best
                if (!availableIndices.isEmpty() && !availableIndices.equals(bestAttackers)
                        && !availableIndices.equals(mustAttackIndices)) {
                    actions.add(new SimulationAction.DeclareAttackers(availableIndices));
                }
            }
            case PendingInteraction.BlockerDeclaration ignored -> {
                // Offer diverse blocking options for MCTS to explore:
                // 1) No blocking at all
                actions.add(new SimulationAction.DeclareBlockers(List.of()));

                // 2) Best blocking from CombatSimulator (greedy heuristic)
                List<int[]> bestBlockers = findBestBlockerAssignments(gd, playerId);
                if (!bestBlockers.isEmpty()) {
                    actions.add(new SimulationAction.DeclareBlockers(bestBlockers));
                }

                // 3) Individual "block only the biggest attacker" per blocker —
                //    lets MCTS discover partial blocking strategies (e.g. trade one
                //    blocker on the biggest threat, keep others for next turn).
                UUID oppId = getOpponentId(gd, playerId);
                List<Permanent> oppBf = gd.playerBattlefields.getOrDefault(oppId, List.of());
                List<Permanent> aiBf = gd.playerBattlefields.getOrDefault(playerId, List.of());
                // Find the biggest unblockable-safe attacker (highest power)
                int biggestAttackerIdx = -1;
                int biggestPower = 0;
                for (int i = 0; i < oppBf.size(); i++) {
                    Permanent att = oppBf.get(i);
                    if (att.isAttacking()) {
                        int power = gameQueryService.getEffectivePower(gd, att);
                        if (power > biggestPower) {
                            biggestPower = power;
                            biggestAttackerIdx = i;
                        }
                    }
                }
                if (biggestAttackerIdx >= 0) {
                    // For each available blocker, offer "block only the biggest attacker"
                    for (int bi = 0; bi < aiBf.size(); bi++) {
                        if (!gameQueryService.canBlock(gd, aiBf.get(bi))) continue;
                        List<int[]> singleBlock = List.of(new int[]{bi, biggestAttackerIdx});
                        // Avoid duplicating an already-added option
                        if (!singleBlock.equals(bestBlockers)) {
                            actions.add(new SimulationAction.DeclareBlockers(singleBlock));
                        }
                    }
                }
            }
            case PendingInteraction.HandCardChoice cardChoice -> addHandChoiceActions(actions, cardChoice);
            case PendingInteraction.DiscardChoice cardChoice -> addHandChoiceActions(actions, cardChoice);
            case PendingInteraction.RevealedHandChoice rhc -> {
                if (rhc.validIndices() != null) {
                    for (int idx : rhc.validIndices()) {
                        actions.add(new SimulationAction.ChooseCard(idx));
                    }
                }
            }
            case PendingInteraction.PermanentChoice permChoice -> {
                for (UUID id : permChoice.validIds()) {
                    actions.add(new SimulationAction.ChoosePermanent(id));
                }
            }
            case PendingInteraction.ColorChoice cc -> {
                if (cc.context() instanceof ChoiceContext.KeywordGrantChoice kgc) {
                    for (var kw : kgc.options()) {
                        actions.add(new SimulationAction.ChooseColor(kw.name()));
                    }
                } else {
                    actions.add(new SimulationAction.ChooseColor("WHITE"));
                    actions.add(new SimulationAction.ChooseColor("BLUE"));
                    actions.add(new SimulationAction.ChooseColor("BLACK"));
                    actions.add(new SimulationAction.ChooseColor("RED"));
                    actions.add(new SimulationAction.ChooseColor("GREEN"));
                }
            }
            case PendingInteraction.MayAbilityChoice ignored -> {
                actions.add(new SimulationAction.MayAbilityChoice(true));
                actions.add(new SimulationAction.MayAbilityChoice(false));
            }
            default -> {
                // For complex interactions (library search, reorder, etc.), use heuristic
                actions.add(new SimulationAction.PassPriority());
            }
        }

        if (actions.isEmpty()) {
            actions.add(new SimulationAction.PassPriority());
        }
        return actions;
    }

    /**
     * Applies an action to the game state. The game state is mutated in place.
     * After applying, auto-resolves any pending decisions for other players.
     */
    public void applyAction(GameData gd, UUID playerId, SimulationAction action) {
        Player player = new Player(playerId, gd.playerIdToName.getOrDefault(playerId, "AI"));

        try {
            synchronized (gd) {
                switch (action) {
                    case SimulationAction.PlayCard pc -> {
                        Card card = gd.playerHands.get(playerId).get(pc.handIndex());
                        tapLandsForCard(gd, playerId, card, pc.xValue());
                        List<Integer> exileIndices = computeExileNGraveyardIndices(gd, playerId, card);
                        UUID sacrificeId = computeSacrificeTarget(gd, playerId, card);
                        if (exileIndices != null || sacrificeId != null) {
                            gameService.playCard(gd, player, pc.handIndex(), pc.xValue(), pc.targetId(),
                                    null, List.of(), List.of(), false, sacrificeId, null, null, null, exileIndices);
                        } else {
                            gameService.playCard(gd, player, pc.handIndex(), pc.xValue(), pc.targetId(), null);
                        }
                    }
                    case SimulationAction.PassPriority ignored ->
                            gameService.passPriority(gd, player);
                    case SimulationAction.DeclareAttackers da ->
                            gameService.declareAttackers(gd, player, da.attackerIndices(), null);
                    case SimulationAction.DeclareBlockers db -> {
                        List<BlockerAssignment> assignments = db.blockerAssignments().stream()
                                .map(a -> new BlockerAssignment(a[0], a[1]))
                                .toList();
                        gameService.declareBlockers(gd, player, assignments);
                    }
                    case SimulationAction.ChooseCard cc ->
                            gameService.handleCardChosen(gd, player, cc.cardIndex());
                    case SimulationAction.ChoosePermanent cp ->
                            gameService.handlePermanentChosen(gd, player, cp.permanentId());
                    case SimulationAction.ChooseColor col ->
                            gameService.handleListChoice(gd, player, col.color());
                    case SimulationAction.MayAbilityChoice mac ->
                            gameService.handleMayAbilityChosen(gd, player, mac.accept());
                    case SimulationAction.ActivateAbility aa ->
                            gameService.activateAbility(gd, player, findPermanentIndex(gd, playerId, aa.permanentId()),
                                    aa.abilityIndex(), 0, aa.targetId(), null);
                }
            }
        } catch (Exception e) {
            // Simulation may hit edge cases — swallow and continue
            log.trace("Simulation action failed: {}", e.getMessage());
        }

        // Auto-resolve pending decisions for any player (including opponent)
        autoResolveDecisions(gd, playerId, 10);
    }

    /**
     * Returns true if the game is over (finished or a player is at 0 or less life).
     */
    public boolean isTerminal(GameData gd) {
        if (gd.status == GameStatus.FINISHED) return true;
        for (UUID pid : gd.orderedPlayerIds) {
            if (gd.getLife(pid) <= 0) return true;
        }
        return false;
    }

    /**
     * Evaluates the game state from the AI's perspective, normalized to [0, 1].
     * 1.0 = AI wins, 0.0 = opponent wins, 0.5 = even.
     */
    public double evaluate(GameData gd, UUID aiPlayerId) {
        double raw = boardEvaluator.evaluate(gd, aiPlayerId);
        // Normalize using sigmoid-like function: map (-inf, inf) to (0, 1)
        return 1.0 / (1.0 + Math.exp(-raw / 50.0));
    }

    public GameQueryService getGameQueryService() {
        return gameQueryService;
    }

    public BoardEvaluator getBoardEvaluator() {
        return boardEvaluator;
    }

    public SpellEvaluator getSpellEvaluator() {
        return spellEvaluator;
    }

    public CombatSimulator getCombatSimulator() {
        return combatSimulator;
    }

    public GameService getGameService() {
        return gameService;
    }

    public GameRegistry getGameRegistry() {
        return gameRegistry;
    }

    // ===== Private helpers =====

    /**
     * Auto-resolves any pending interactions using heuristic decisions.
     * Loops until no more decisions are pending or max iterations reached.
     */
    private void autoResolveDecisions(GameData gd, UUID mctsPlayerId, int maxIterations) {
        for (int i = 0; i < maxIterations; i++) {
            if (isTerminal(gd)) return;

            PendingInteraction awaiting = gd.interaction.activeInteraction();
            if (awaiting == null) {
                // Check if we need to pass priority for the non-MCTS player
                UUID priorityHolder = getPriorityPlayer(gd);
                if (priorityHolder != null && !priorityHolder.equals(mctsPlayerId)) {
                    Player oppPlayer = new Player(priorityHolder, "opp");
                    try {
                        synchronized (gd) {
                            gameService.passPriority(gd, oppPlayer);
                        }
                    } catch (Exception e) {
                        return;
                    }
                    continue;
                }
                return; // MCTS player's turn to decide
            }

            // Determine which player the interaction is for
            UUID interactionPlayer = getInteractionPlayer(gd);
            if (interactionPlayer == null) return;

            Player resolvePlayer = new Player(interactionPlayer, gd.playerIdToName.getOrDefault(interactionPlayer, "AI"));

            try {
                synchronized (gd) {
                    resolveInteraction(gd, resolvePlayer, awaiting, mctsPlayerId);
                }
            } catch (Exception e) {
                log.trace("Auto-resolve failed: {}", e.getMessage());
                return;
            }
        }
    }

    private void resolveInteraction(GameData gd, Player player, PendingInteraction awaiting, UUID mctsPlayerId) {
        switch (awaiting) {
            case PendingInteraction.AttackerDeclaration ignored -> {
                UUID pid = player.getId();
                List<Permanent> battlefield = gd.playerBattlefields.getOrDefault(pid, List.of());
                List<Integer> available = new ArrayList<>();
                for (int i = 0; i < battlefield.size(); i++) {
                    Permanent perm = battlefield.get(i);
                    if (!gameQueryService.isCreature(gd, perm)) continue;
                    if (perm.isTapped()) continue;
                    if (perm.isSummoningSick() && !gameQueryService.hasKeyword(gd, perm, Keyword.HASTE)) continue;
                    if (gameQueryService.hasKeyword(gd, perm, Keyword.DEFENDER)) continue;
                    available.add(i);
                }
                List<Integer> mustAttack = combatAttackService.getMustAttackIndices(gd, pid, available);
                List<Integer> attackers = combatSimulator.findBestAttackers(gd, pid, available, mustAttack);
                // Ensure at least one attacker when forced (e.g. Trove of Temptation)
                if (attackers.isEmpty() && !available.isEmpty()
                        && combatAttackService.isOpponentForcedToAttack(gd, pid)) {
                    attackers = List.of(available.getFirst());
                }
                gameService.declareAttackers(gd, player, attackers, null);
            }
            case PendingInteraction.BlockerDeclaration ignored -> {
                List<int[]> blockers = findBestBlockerAssignments(gd, player.getId());
                List<BlockerAssignment> assignments = blockers.stream()
                        .map(a -> new BlockerAssignment(a[0], a[1]))
                        .toList();
                gameService.declareBlockers(gd, player, assignments);
            }
            case PendingInteraction.HandCardChoice cc -> resolveHandCardChoice(gd, player, cc);
            case PendingInteraction.DiscardChoice cc -> resolveHandCardChoice(gd, player, cc);
            case PendingInteraction.PermanentChoice pc -> {
                if (!pc.validIds().isEmpty()) {
                    UUID chosen = pc.validIds().iterator().next();
                    gameService.handlePermanentChosen(gd, player, chosen);
                }
            }
            case PendingInteraction.ColorChoice ccCtx -> {
                if (ccCtx.context() instanceof ChoiceContext.KeywordGrantChoice kgc) {
                    gameService.handleListChoice(gd, player, kgc.options().getFirst().name());
                } else {
                    gameService.handleListChoice(gd, player, "RED");
                }
            }
            case PendingInteraction.MayAbilityChoice ignored -> gameService.handleMayAbilityChosen(gd, player, true);
            case PendingInteraction.GraveyardChoice gc -> {
                if (gc.validIndices() != null && !gc.validIndices().isEmpty()) {
                    gameService.handleGraveyardCardChosen(gd, player, gc.validIndices().iterator().next());
                }
            }
            case PendingInteraction.GraveyardExileCostChoice gec -> {
                if (gec.validIndices() != null && !gec.validIndices().isEmpty()) {
                    gameService.handleGraveyardCardChosen(gd, player, gec.validIndices().iterator().next());
                }
            }
            case PendingInteraction.MultiPermanentChoice mpc -> {
                if (mpc.validIds() != null && !mpc.validIds().isEmpty()) {
                    List<UUID> chosen = mpc.validIds().stream().limit(mpc.maxCount()).toList();
                    gameService.handleMultiplePermanentsChosen(gd, player, chosen);
                }
            }
            case PendingInteraction.MultiGraveyardChoice mgc -> {
                if (!mgc.validCardIds().isEmpty()) {
                    List<UUID> chosen = mgc.validCardIds().stream().limit(mgc.maxCount()).toList();
                    gameService.handleMultipleCardsChosen(gd, player, chosen);
                }
            }
            case PendingInteraction.MultiZoneExileChoice mzec -> {
                if (mzec.validCardIds() != null && !mzec.validCardIds().isEmpty()) {
                    List<UUID> chosen = new ArrayList<>(mzec.validCardIds());
                    gameService.handleMultipleCardsChosen(gd, player, chosen);
                }
            }
            case PendingInteraction.MirrorOfFateChoice mfc -> {
                if (mfc.validCardIds() != null && !mfc.validCardIds().isEmpty()) {
                    List<UUID> chosen = mfc.validCardIds().stream().limit(mfc.maxCount()).toList();
                    gameService.handleMultipleCardsChosen(gd, player, chosen);
                }
            }
            case PendingInteraction.CombatDamageAssignment cda -> {
                Map<UUID, Integer> assignments = autoAssignCombatDamage(cda);
                gameService.handleCombatDamageAssigned(gd, player, cda.attackerIndex(), assignments);
            }
            case PendingInteraction.LibrarySearch ls -> {
                if (ls.params().cards() != null && !ls.params().cards().isEmpty()) {
                    gameService.handleLibraryCardChosen(gd, player, 0);
                }
            }
            case PendingInteraction.Scry sc -> {
                if (sc.cards() != null) {
                    List<Integer> topOrder = new ArrayList<>();
                    for (int k = 0; k < sc.cards().size(); k++) topOrder.add(k);
                    gameService.handleScryCompleted(gd, player, topOrder, List.of());
                }
            }
            case PendingInteraction.LibraryReorder lr -> {
                if (lr.cards() != null) {
                    List<Integer> order = new ArrayList<>();
                    for (int k = 0; k < lr.cards().size(); k++) order.add(k);
                    gameService.handleLibraryCardsReordered(gd, player, order);
                }
            }
            case PendingInteraction.RevealedHandChoice rhc -> {
                if (rhc.validIndices() != null && !rhc.validIndices().isEmpty()) {
                    gameService.handleCardChosen(gd, player, rhc.validIndices().iterator().next());
                }
            }
            case PendingInteraction.RevealCardsDiscardChoice rcdc -> {
                if (rcdc.validIndices() != null && !rcdc.validIndices().isEmpty()) {
                    gameService.handleCardChosen(gd, player, rcdc.validIndices().iterator().next());
                }
            }
            case PendingInteraction.HandTopBottomChoice ignored -> gameService.handleHandTopBottomChosen(gd, player, 0, 1);
            case PendingInteraction.LibraryRevealChoice lrc -> {
                if (lrc.validCardIds() != null && !lrc.validCardIds().isEmpty()) {
                    if (lrc.lifeCostPerSelection() > 0) {
                        // Punisher reveal (e.g. Sword-Point Diplomacy): deny nothing (don't pay life)
                        gameService.handleMultipleCardsChosen(gd, player, List.of());
                    } else {
                        // Normal library reveal: choose all valid cards
                        gameService.handleMultipleCardsChosen(gd, player, new ArrayList<>(lrc.validCardIds()));
                    }
                }
            }
            default -> {
                // Unknown interaction — skip
            }
        }
    }

    private static void addHandChoiceActions(List<SimulationAction> actions, PendingInteraction.HandChoice cardChoice) {
        if (cardChoice.validIndices() != null) {
            for (int idx : cardChoice.validIndices()) {
                actions.add(new SimulationAction.ChooseCard(idx));
            }
        }
    }

    private void resolveHandCardChoice(GameData gd, Player player, PendingInteraction.HandChoice cc) {
        if (cc.validIndices() != null && !cc.validIndices().isEmpty()) {
            // Pick lowest value card
            List<Card> hand = gd.playerHands.get(player.getId());
            int bestIdx = cc.validIndices().iterator().next();
            if (hand != null) {
                bestIdx = cc.validIndices().stream()
                        .min(Comparator.comparingDouble(idx ->
                                spellEvaluator.estimateSpellValue(gd, hand.get(idx), player.getId())))
                        .orElse(bestIdx);
            }
            gameService.handleCardChosen(gd, player, bestIdx);
        }
    }

    private UUID getInteractionPlayer(GameData gd) {
        // The active interaction record carries the decider
        PendingInteraction active = gd.interaction.activeInteraction();
        if (active != null) {
            return switch (active) {
                case PendingInteraction.XValueChoice xvc -> xvc.playerId();
                case PendingInteraction.Scry s -> s.playerId();
                case PendingInteraction.HandTopBottomChoice htbc -> htbc.playerId();
                case PendingInteraction.LibraryReorder lr -> lr.playerId();
                case PendingInteraction.MayAbilityChoice mc -> mc.playerId();
                case PendingInteraction.KnowledgePoolCastChoice kpc -> kpc.playerId();
                case PendingInteraction.MirrorOfFateChoice mfc -> mfc.playerId();
                case PendingInteraction.MultiZoneExileChoice mzec -> mzec.playerId();
                case PendingInteraction.MultiPermanentChoice mpc -> mpc.playerId();
                case PendingInteraction.MultiGraveyardChoice mgc -> mgc.playerId();
                case PendingInteraction.ColorChoice cc -> cc.playerId();
                case PendingInteraction.RevealedHandChoice rhc -> rhc.choosingPlayerId();
                case PendingInteraction.RevealCardsDiscardChoice rcdc -> rcdc.decidingPlayerId();
                case PendingInteraction.GraveyardChoice gc -> gc.playerId();
                case PendingInteraction.GraveyardExileCostChoice gec -> gec.playerId();
                case PendingInteraction.HandChoice hc -> hc.playerId();
                case PendingInteraction.LibraryRevealChoice lrc -> lrc.playerId();
                case PendingInteraction.LibrarySearch ls -> ls.params().playerId();
                case PendingInteraction.PermanentChoice pc -> pc.playerId();
                case PendingInteraction.CombatDamageAssignment cda -> cda.playerId();
                case PendingInteraction.AttackerDeclaration ad -> ad.activePlayerId();
                case PendingInteraction.BlockerDeclaration bd -> bd.defenderId();
                default -> null;
            };
        }
        return null;
    }

    private UUID getPriorityPlayer(GameData gd) {
        if (gd.activePlayerId == null) return null;
        if (!gd.priorityPassedBy.contains(gd.activePlayerId)) {
            return gd.activePlayerId;
        }
        for (UUID id : gd.orderedPlayerIds) {
            if (!id.equals(gd.activePlayerId) && !gd.priorityPassedBy.contains(id)) {
                return id;
            }
        }
        return null;
    }

    private List<int[]> findBestBlockerAssignments(GameData gd, UUID playerId) {
        UUID opponentId = getOpponentId(gd, playerId);
        List<Permanent> oppBattlefield = gd.playerBattlefields.getOrDefault(opponentId, List.of());
        List<Permanent> battlefield = gd.playerBattlefields.getOrDefault(playerId, List.of());

        List<Integer> attackerIndices = new ArrayList<>();
        for (int i = 0; i < oppBattlefield.size(); i++) {
            if (oppBattlefield.get(i).isAttacking()) attackerIndices.add(i);
        }
        List<Integer> blockerIndices = new ArrayList<>();
        for (int i = 0; i < battlefield.size(); i++) {
            if (gameQueryService.canBlock(gd, battlefield.get(i))) {
                blockerIndices.add(i);
            }
        }
        return combatSimulator.findBestBlockers(gd, playerId, attackerIndices, blockerIndices);
    }

    private void tapLandsForCard(GameData gd, UUID playerId, Card card, int xValue) {
        if (card.getManaCost() == null) return;
        ManaCost cost = new ManaCost(card.getManaCost());
        ManaPool currentPool = gd.playerManaPools.get(playerId);
        Player player = new Player(playerId, "sim");

        if (card.isRequiresCreatureMana()) {
            if (cost.canPayCreatureOnly(currentPool)) return;
            List<Permanent> battlefield = gd.playerBattlefields.getOrDefault(playerId, List.of());
            for (int i = 0; i < battlefield.size(); i++) {
                Permanent perm = battlefield.get(i);
                if (perm.isTapped()) continue;
                if (!gameQueryService.isCreature(gd, perm)) continue;
                if (perm.isSummoningSick()
                        && !gameQueryService.hasKeyword(gd, perm, Keyword.HASTE)) continue;
                if (!tapOrActivateMana(gd, player, perm, i, cost, currentPool)) continue;
                currentPool = gd.playerManaPools.get(playerId);
                if (cost.canPayCreatureOnly(currentPool)) return;
            }
            return;
        }

        boolean alreadyPaid;
        if (cost.hasX() && card.getXColorRestriction() != null) {
            alreadyPaid = cost.canPay(currentPool, xValue, card.getXColorRestriction(), 0);
        } else {
            alreadyPaid = cost.canPay(currentPool, xValue);
        }
        if (alreadyPaid) return;

        List<Permanent> battlefield = gd.playerBattlefields.getOrDefault(playerId, List.of());
        for (int i = 0; i < battlefield.size(); i++) {
            Permanent perm = battlefield.get(i);
            if (perm.isTapped()) continue;
            if (gameQueryService.isCreature(gd, perm) && perm.isSummoningSick()
                    && !gameQueryService.hasKeyword(gd, perm, Keyword.HASTE)) continue;
            if (!tapOrActivateMana(gd, player, perm, i, cost, currentPool)) continue;
            currentPool = gd.playerManaPools.get(playerId);
            boolean canPayNow;
            if (cost.hasX() && card.getXColorRestriction() != null) {
                canPayNow = cost.canPay(currentPool, xValue, card.getXColorRestriction(), 0);
            } else {
                canPayNow = cost.canPay(currentPool, xValue);
            }
            if (canPayNow) return;
        }
    }

    /**
     * Computes graveyard card indices to exile for {@link ExileNCardsFromGraveyardCost}.
     * Returns null if the card has no such cost.
     */
    private List<Integer> computeExileNGraveyardIndices(GameData gd, UUID playerId, Card card) {
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            if (effect instanceof ExileNCardsFromGraveyardCost cost) {
                List<Card> graveyard = gd.playerGraveyards.getOrDefault(playerId, List.of());
                List<Integer> matchingIndices = new ArrayList<>();
                for (int i = 0; i < graveyard.size(); i++) {
                    Card c = graveyard.get(i);
                    if (cost.requiredType() == null || c.hasType(cost.requiredType())) {
                        matchingIndices.add(i);
                    }
                }
                if (matchingIndices.size() < cost.count()) return null;
                return new ArrayList<>(matchingIndices.subList(0, cost.count()));
            }
        }
        return null;
    }

    /**
     * Finds the first valid sacrifice target for the card's sacrifice cost.
     * Returns null if the card has no sacrifice cost.
     */
    private UUID computeSacrificeTarget(GameData gd, UUID playerId, Card card) {
        List<Permanent> battlefield = gd.playerBattlefields.getOrDefault(playerId, List.of());
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            if (effect instanceof SacrificeCreatureCost) {
                return battlefield.stream()
                        .filter(p -> gameQueryService.isCreature(gd, p))
                        .findFirst().map(Permanent::getId).orElse(null);
            } else if (effect instanceof SacrificeArtifactCost) {
                return battlefield.stream()
                        .filter(p -> gameQueryService.isArtifact(gd, p))
                        .findFirst().map(Permanent::getId).orElse(null);
            } else if (effect instanceof SacrificePermanentCost sacCost) {
                return battlefield.stream()
                        .filter(p -> predicateEvaluationService.matchesPermanentPredicate(gd, p, sacCost.filter()))
                        .findFirst().map(Permanent::getId).orElse(null);
            }
        }
        return null;
    }

    /**
     * Taps a permanent for mana or activates its mana ability in simulation.
     * Returns true if the permanent was tapped, false if it has no mana production.
     */
    private boolean tapOrActivateMana(GameData gd, Player player, Permanent perm, int index, ManaCost cost, ManaPool currentPool) {
        boolean hasOnTapMana = perm.getCard().getEffects(EffectSlot.ON_TAP).stream()
                .anyMatch(e -> e instanceof AwardManaEffect || e instanceof AwardAnyColorManaEffect || e instanceof AwardAnyColorChosenSubtypeCreatureManaEffect);
        if (hasOnTapMana) {
            gameService.tapPermanent(gd, player, index);
            return true;
        }
        // Check activated mana abilities (dual lands, pain lands, etc.)
        var abilities = perm.getCard().getActivatedAbilities();
        Integer bestIndex = null;
        int bestScore = -1;
        for (int j = 0; j < abilities.size(); j++) {
            if (!AiManaManager.isFreeTapManaAbility(abilities.get(j))) continue;
            int score = scoreAbilityForSim(abilities.get(j), cost, currentPool);
            if (score > bestScore) {
                bestScore = score;
                bestIndex = j;
            }
        }
        if (bestIndex == null) return false;
        gameService.activateAbility(gd, player, index, bestIndex, null, null, null);
        return true;
    }

    private int scoreAbilityForSim(com.github.laxika.magicalvibes.model.ActivatedAbility ability, ManaCost cost, ManaPool pool) {
        boolean hasSideEffects = ability.getEffects().stream()
                .anyMatch(e -> e instanceof com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect dmg
                        && dmg.recipient() == com.github.laxika.magicalvibes.model.effect.DamageRecipient.CONTROLLER);
        var coloredCosts = cost.getColoredCosts();
        for (CardEffect effect : ability.getEffects()) {
            if (effect instanceof AwardManaEffect award) {
                int needed = coloredCosts.getOrDefault(award.color(), 0);
                int have = pool.get(award.color());
                if (needed > have) return hasSideEffects ? 15 : 20;
                return hasSideEffects ? 1 : 5;
            }
            if (effect instanceof AwardAnyColorManaEffect) return hasSideEffects ? 1 : 5;
        }
        return 0;
    }

    private int calculateSmartX(GameData gd, Card card, UUID targetId, ManaPool virtualPool) {
        ManaCost cost = new ManaCost(card.getManaCost());
        int costModifier = castingCostService.getCastCostModifier(gd, gd.activePlayerId, card);
        int maxX;
        if (card.getXColorRestriction() != null) {
            maxX = cost.calculateMaxX(virtualPool, card.getXColorRestriction(), costModifier);
        } else {
            maxX = Math.max(0, cost.calculateMaxX(virtualPool) - costModifier);
        }
        if (maxX <= 0) {
            return 0;
        }

        if (targetId != null) {
            Permanent target = gameQueryService.findPermanentById(gd, targetId);
            if (target != null && gameQueryService.isCreature(gd, target)) {
                int toughness = gameQueryService.getEffectiveToughness(gd, target);
                return Math.min(toughness, maxX);
            }
        }

        return maxX;
    }

    private UUID findBestTarget(GameData gd, Card card, UUID playerId) {
        UUID opponentId = getOpponentId(gd, playerId);

        // Handle player-only targeting (e.g. Haunting Echoes, Mind Rot)
        Set<TargetType> allowedTargets = EffectResolution.computeAllowedTargets(card);
        if (allowedTargets.contains(TargetType.PLAYER) && !allowedTargets.contains(TargetType.PERMANENT)) {
            return opponentId;
        }

        // Handle graveyard targeting (e.g. Unburial Rites, Gruesome Encore)
        if (allowedTargets.contains(TargetType.GRAVEYARD)) {
            return findBestGraveyardTarget(gd, card, playerId, opponentId);
        }

        // Handle auras — beneficial auras target own creatures, detrimental target opponent's
        if (card.isAura()) {
            boolean isBeneficial = false;
            for (CardEffect effect : card.getEffects(EffectSlot.STATIC)) {
                if ((effect instanceof StaticBoostEffect boost
                        && (boost.scope() == GrantScope.ENCHANTED_CREATURE || boost.scope() == GrantScope.EQUIPPED_CREATURE))
                        || (effect instanceof GrantKeywordEffect grant && grant.scope() == GrantScope.ENCHANTED_CREATURE)) {
                    isBeneficial = true;
                    break;
                }
            }
            if (isBeneficial) {
                return gd.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                        .filter(p -> gameQueryService.isCreature(gd, p))
                        .filter(p -> passesTargetFilter(gd, card, p, playerId))
                        .max(Comparator.comparingInt(p -> gameQueryService.getEffectiveToughness(gd, p)))
                        .map(Permanent::getId)
                        .orElse(null);
            }
            // Detrimental aura — fall through to opponent's battlefield targeting below
        }

        List<Permanent> oppBattlefield = gd.playerBattlefields.getOrDefault(opponentId, List.of());

        // Prefer creatures that pass the target filter
        UUID creatureTarget = oppBattlefield.stream()
                .filter(p -> gameQueryService.isCreature(gd, p))
                .filter(p -> passesTargetFilter(gd, card, p, playerId))
                .max(Comparator.comparingInt(p -> gameQueryService.getEffectivePower(gd, p)))
                .map(Permanent::getId)
                .orElse(null);
        if (creatureTarget != null) {
            return creatureTarget;
        }

        // Fall back to any permanent that passes the target filter (e.g., artifacts/enchantments for Naturalize)
        return oppBattlefield.stream()
                .filter(p -> passesTargetFilter(gd, card, p, playerId))
                .findFirst()
                .map(Permanent::getId)
                .orElse(null);
    }

    private UUID findBestGraveyardTarget(GameData gd, Card card, UUID playerId, UUID opponentId) {
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            if (!effect.canTargetGraveyard()) continue;

            List<Card> candidates;
            if (effect instanceof ReturnCardFromGraveyardEffect rge) {
                candidates = getSimGraveyardCandidates(gd, rge.source(), playerId, opponentId);
                if (rge.filter() != null) {
                    candidates = candidates.stream()
                            .filter(c -> predicateEvaluationService.matchesCardPredicate(c, rge.filter(), card.getId()))
                            .toList();
                }
            } else {
                GraveyardSearchScope scope = effect.canTargetAnyGraveyard()
                        ? GraveyardSearchScope.ALL_GRAVEYARDS
                        : GraveyardSearchScope.OPPONENT_GRAVEYARD;
                candidates = getSimGraveyardCandidates(gd, scope, playerId, opponentId);
            }

            if (!candidates.isEmpty()) {
                return candidates.stream()
                        .max(Comparator.comparingInt(Card::getManaValue))
                        .map(Card::getId)
                        .orElse(null);
            }
        }
        return null;
    }

    private List<Card> getSimGraveyardCandidates(GameData gd, GraveyardSearchScope scope,
                                                  UUID playerId, UUID opponentId) {
        List<Card> candidates = new ArrayList<>();
        switch (scope) {
            case CONTROLLERS_GRAVEYARD -> candidates.addAll(
                    gd.playerGraveyards.getOrDefault(playerId, List.of()));
            case OPPONENT_GRAVEYARD -> candidates.addAll(
                    gd.playerGraveyards.getOrDefault(opponentId, List.of()));
            case ALL_GRAVEYARDS -> {
                for (UUID pid : gd.orderedPlayerIds) {
                    candidates.addAll(gd.playerGraveyards.getOrDefault(pid, List.of()));
                }
            }
        }
        return candidates;
    }

    private boolean passesTargetFilter(GameData gd, Card card, Permanent target, UUID controllerId) {
        if (card.getTargetFilter() == null) {
            return true;
        }
        try {
            predicateEvaluationService.validateTargetFilter(card.getTargetFilter(), target,
                    FilterContext.of(gd)
                            .withSourceCardId(card.getId())
                            .withSourceControllerId(controllerId));
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    private int findPermanentIndex(GameData gd, UUID playerId, UUID permanentId) {
        List<Permanent> battlefield = gd.playerBattlefields.getOrDefault(playerId, List.of());
        for (int i = 0; i < battlefield.size(); i++) {
            if (battlefield.get(i).getId().equals(permanentId)) return i;
        }
        return -1;
    }

    private Map<UUID, Integer> autoAssignCombatDamage(PendingInteraction.CombatDamageAssignment cda) {
        Map<UUID, Integer> assignments = new HashMap<>();
        int remaining = cda.totalDamage();
        for (var target : cda.validTargets()) {
            if (target.isPlayer()) continue;
            int lethal = cda.isDeathtouch()
                    ? Math.max(0, 1 - target.currentDamage())
                    : target.effectiveToughness() - target.currentDamage();
            int dmg = Math.min(remaining, lethal);
            if (dmg > 0) {
                assignments.put(target.id(), dmg);
                remaining -= dmg;
            }
        }
        if (remaining > 0) {
            for (var target : cda.validTargets()) {
                if (target.isPlayer()) {
                    assignments.put(target.id(), remaining);
                    break;
                }
            }
        }
        return assignments;
    }

    private UUID getOpponentId(GameData gd, UUID playerId) {
        for (UUID id : gd.orderedPlayerIds) {
            if (!id.equals(playerId)) return id;
        }
        return null;
    }
}
