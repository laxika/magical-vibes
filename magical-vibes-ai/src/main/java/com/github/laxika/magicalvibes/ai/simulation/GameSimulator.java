package com.github.laxika.magicalvibes.ai.simulation;

import com.github.laxika.magicalvibes.ai.AiManaManager;
import com.github.laxika.magicalvibes.ai.BoardEvaluator;
import com.github.laxika.magicalvibes.ai.CombatDamageAssignmentHeuristic;
import com.github.laxika.magicalvibes.ai.CombatSimulator;
import com.github.laxika.magicalvibes.ai.SizeGatedRemovalPump;
import com.github.laxika.magicalvibes.ai.SpellEvaluator;
import com.github.laxika.magicalvibes.ai.TargetPolarity;
import com.github.laxika.magicalvibes.ai.TargetPolarityClassifier;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.InteractionOptions;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TargetType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.AddManaOnEnchantedLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatMainPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCategory;
import com.github.laxika.magicalvibes.model.effect.CostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.KeywordGrantingEffect;
import com.github.laxika.magicalvibes.model.effect.ManaProducingEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.StaticCreatureBoostEffect;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.service.combat.CombatAttackService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.GameService;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

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

    /**
     * Budget of greedy opponent creature casts per simulation state. Unbounded
     * development halves MCTS throughput (every extra creature inflates the combat
     * simulations in each rollout) and drowns the root-action reward signal in
     * determinization noise — measured to flip correct casts into passes at the
     * production time budget. The budget must also be independent of the MCTS
     * player's actions (e.g. a board-emptiness condition would punish exactly the
     * board-clearing plays), so it is a flat per-simulation-state allowance.
     */
    private static final int MAX_OPPONENT_CASTS_PER_ROLLOUT = 1;

    /**
     * Tracks the opponent cast budget per {@link GameData}. Keyed weakly by the
     * determinized copy each MCTS iteration works on, so the budget resets for
     * every rollout and entries vanish with the discarded copies.
     */
    private final Map<GameData, Integer> opponentRolloutCasts =
            Collections.synchronizedMap(new WeakHashMap<>());

    /**
     * Cap on enumerated targets per spell and on double-block pair options. MCTS
     * runs on a time budget, so excessive root branching dilutes visits per action;
     * these caps keep the added choices (top creatures + face, cheapest sufficient
     * double-blocks) without flooding the tree.
     */
    private static final int MAX_TARGET_CANDIDATES = 3;
    private static final int MAX_DOUBLE_BLOCK_PAIRS = 3;
    /** Caps list-pick enumeration so huge lists (e.g. card-name choices) don't blow up the tree. */
    private static final int MAX_LIST_OPTIONS = 8;

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
    private final TargetPolarityClassifier polarityClassifier;
    private final AmountEvaluationService amountEvaluationService;

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
        this.amountEvaluationService = new AmountEvaluationService(predicateEvaluationService, gameQueryService);
        this.polarityClassifier = new TargetPolarityClassifier(amountEvaluationService);
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
                actions.addAll(enumerateCastableSpells(gd, playerId));
            }
            // Always can pass priority
            actions.add(new SimulationAction.PassPriority());
            return actions;
        }

        switch (awaitingInput) {
            case PendingInteraction.AttackerDeclaration ignored -> {
                List<Integer> availableIndices = combatAttackService.getAttackableCreatureIndices(gd, playerId);
                List<Integer> mustAttackIndices = combatAttackService.getMustAttackIndices(gd, playerId, availableIndices);
                // "All-in" excludes creatures that assign no combat damage (power <= 0, CR 510.1a)
                List<Integer> allInIndices = combatSimulator.filterZeroPowerAttackers(
                        gd, playerId, availableIndices, mustAttackIndices);
                // Use CombatSimulator to find best attackers, then also offer empty/must-only attack
                List<Integer> bestAttackers = combatSimulator.findBestAttackers(gd, playerId, availableIndices, mustAttackIndices);
                boolean forcedToAttack = combatAttackService.isOpponentForcedToAttack(gd, playerId);
                if (mustAttackIndices.isEmpty() && !forcedToAttack) {
                    actions.add(new SimulationAction.DeclareAttackers(List.of())); // no attack
                } else if (mustAttackIndices.isEmpty() && forcedToAttack) {
                    // Forced to attack with at least one — offer the first worth sending
                    List<Integer> forcedPool = allInIndices.isEmpty() ? availableIndices : allInIndices;
                    actions.add(new SimulationAction.DeclareAttackers(List.of(forcedPool.getFirst())));
                } else {
                    // Must-attack creatures must always be included
                    actions.add(new SimulationAction.DeclareAttackers(mustAttackIndices));
                }
                if (!bestAttackers.isEmpty() && !bestAttackers.equals(mustAttackIndices)) {
                    actions.add(new SimulationAction.DeclareAttackers(bestAttackers));
                }
                // Also try all-in attack if different from best
                if (!allInIndices.isEmpty() && !allInIndices.equals(bestAttackers)
                        && !allInIndices.equals(mustAttackIndices)) {
                    actions.add(new SimulationAction.DeclareAttackers(allInIndices));
                }
                // Also try holding back the biggest creature as a defender: the best
                // set minus its highest-power member (must-attackers stay included).
                if (bestAttackers.size() >= 2) {
                    List<Permanent> battlefield = gd.playerBattlefields.getOrDefault(playerId, List.of());
                    int holdBackIdx = -1;
                    int holdBackPower = -1;
                    for (int idx : bestAttackers) {
                        if (mustAttackIndices.contains(idx)) continue;
                        int power = gameQueryService.getEffectivePower(gd, battlefield.get(idx));
                        if (power > holdBackPower) {
                            holdBackPower = power;
                            holdBackIdx = idx;
                        }
                    }
                    if (holdBackIdx >= 0) {
                        List<Integer> holdBack = new ArrayList<>(bestAttackers);
                        holdBack.remove(Integer.valueOf(holdBackIdx));
                        boolean duplicate = actions.stream()
                                .anyMatch(a -> a instanceof SimulationAction.DeclareAttackers da
                                        && da.attackerIndices().equals(holdBack));
                        if (!duplicate) {
                            actions.add(new SimulationAction.DeclareAttackers(holdBack));
                        }
                    }
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
                    List<Integer> availableBlockers = new ArrayList<>();
                    for (int bi = 0; bi < aiBf.size(); bi++) {
                        if (gameQueryService.canBlock(gd, aiBf.get(bi))) availableBlockers.add(bi);
                    }

                    // For each available blocker, offer "block only the biggest attacker"
                    for (int bi : availableBlockers) {
                        List<int[]> singleBlock = List.of(new int[]{bi, biggestAttackerIdx});
                        // Avoid duplicating an already-added option
                        if (!sameAssignments(singleBlock, bestBlockers)) {
                            actions.add(new SimulationAction.DeclareBlockers(singleBlock));
                        }
                    }

                    // 4) Double-blocks ganging up on the biggest attacker — the greedy
                    //    assignment never proposes them, and against menace they are the
                    //    only legal way to block it at all. Prefer the cheapest pair
                    //    whose combined power still kills it.
                    int attackerToughness = gameQueryService.getEffectiveToughness(gd, oppBf.get(biggestAttackerIdx));
                    List<int[]> killingPairs = new ArrayList<>(); // {blocker1, blocker2, combinedPower}
                    for (int i = 0; i < availableBlockers.size(); i++) {
                        for (int j = i + 1; j < availableBlockers.size(); j++) {
                            int b1 = availableBlockers.get(i);
                            int b2 = availableBlockers.get(j);
                            int combined = gameQueryService.getEffectivePower(gd, aiBf.get(b1))
                                    + gameQueryService.getEffectivePower(gd, aiBf.get(b2));
                            if (combined >= attackerToughness) {
                                killingPairs.add(new int[]{b1, b2, combined});
                            }
                        }
                    }
                    killingPairs.sort(Comparator.comparingInt(pair -> pair[2]));
                    int doubleBlocksAdded = 0;
                    for (int[] pair : killingPairs) {
                        if (doubleBlocksAdded >= MAX_DOUBLE_BLOCK_PAIRS) break;
                        List<int[]> doubleBlock = List.of(
                                new int[]{pair[0], biggestAttackerIdx},
                                new int[]{pair[1], biggestAttackerIdx});
                        if (sameAssignments(doubleBlock, bestBlockers)) continue;
                        actions.add(new SimulationAction.DeclareBlockers(doubleBlock));
                        doubleBlocksAdded++;
                    }
                }
            }
            // Every other kind enumerates generically from the record's legal options
            // (shapes without a SimulationAction mapping fall through to PassPriority
            // below and get resolved heuristically during rollout).
            default -> addOptionsActions(actions, awaitingInput.legalOptions());
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
                    case SimulationAction.PlayCard pc -> executePlayCard(gd, player, pc);
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
                            gameService.handleInteractionAnswer(gd, player, new InteractionAnswer.CardIndexChosen(cc.cardIndex()));
                    case SimulationAction.ChoosePermanent cp ->
                            gameService.handleInteractionAnswer(gd, player, new InteractionAnswer.PermanentChosen(cp.permanentId()));
                    case SimulationAction.ChooseColor col ->
                            gameService.handleInteractionAnswer(gd, player, new InteractionAnswer.ListChoiceMade(col.color()));
                    case SimulationAction.MayAbilityChoice mac ->
                            gameService.handleInteractionAnswer(gd, player, new InteractionAnswer.MayAbilityChosen(mac.accept()));
                    case SimulationAction.ActivateAbility aa ->
                            gameService.activateAbility(gd, player, findPermanentIndex(gd, playerId, aa.permanentId()),
                                    aa.abilityIndex(), 0, aa.targetId(), null);
                }
            }
        } catch (Exception e) {
            // Simulation may hit edge cases — swallow and continue
            log.trace("Simulation action failed: {}", e.getMessage());
        }

        // Auto-resolve pending decisions for any player (including opponent). The budget
        // covers the simulated opponent's own main-phase land drop and spell casts too.
        autoResolveDecisions(gd, playerId, 30);
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
                    Player oppPlayer = new Player(priorityHolder, gd.playerIdToName.getOrDefault(priorityHolder, "opp"));
                    // Greedy opponent policy: during its own main phase the simulated
                    // opponent plays a land and casts spells instead of always passing,
                    // so rollouts don't see a frozen opponent board.
                    if (isOpponentSorceryWindow(gd, priorityHolder)) {
                        if (tryOpponentPlayLand(gd, oppPlayer)) continue;
                        if (opponentRolloutCasts.getOrDefault(gd, 0) < MAX_OPPONENT_CASTS_PER_ROLLOUT
                                && tryOpponentCastSpell(gd, oppPlayer)) {
                            opponentRolloutCasts.merge(gd, 1, Integer::sum);
                            continue;
                        }
                    }
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

    /**
     * Enumerates the sorcery-speed spells the MCTS player could cast right now —
     * one action per candidate target (capped at {@link #MAX_TARGET_CANDIDATES}),
     * with the X value computed per target.
     */
    private List<SimulationAction.PlayCard> enumerateCastableSpells(GameData gd, UUID playerId) {
        List<SimulationAction.PlayCard> castable = new ArrayList<>();
        List<Card> hand = gd.playerHands.get(playerId);
        if (hand == null) return castable;

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
            // Policy: an extra-combat spell that grants no additional attacks (no
            // surviving attackers to untap, nothing held back after combat) is a dead
            // cast — the few rollout iterations can't reliably punish it, so skip it.
            if (card.getEffects(EffectSlot.SPELL).stream()
                        .anyMatch(AdditionalCombatMainPhaseEffect.class::isInstance)
                    && spellEvaluator.extraCombatDamageGain(gd, card, playerId) <= 0) {
                continue;
            }
            // Policy: "can't block this turn" with no attackers (or no blockers to shut
            // off) is a dead cast — Panic Attack is legal with zero targets, and MCTS
            // would otherwise explore dumping it.
            if (card.getEffects(EffectSlot.SPELL).stream()
                        .anyMatch(CantBlockThisTurnEffect.class::isInstance)
                    && spellEvaluator.estimateSpellValue(gd, card, playerId) <= 0) {
                continue;
            }
            // For targeted spells, enumerate candidate targets (policy: which targets
            // to offer) — one PlayCard per candidate so MCTS can compare targets
            // instead of trusting a fixed heuristic pick.
            if (EffectResolution.needsTarget(card) || card.isAura()) {
                List<UUID> candidates = findCandidateTargets(gd, card, playerId, MAX_TARGET_CANDIDATES);
                if (candidates.isEmpty()) continue; // no valid target
                for (UUID targetId : candidates) {
                    int xValue = 0;
                    if (hasX) {
                        // X depends on the target (lethal X vs a creature, max X to the face)
                        xValue = calculateSmartX(gd, card, targetId, virtualPool);
                        if (xValue <= 0) continue;
                    }
                    castable.add(new SimulationAction.PlayCard(i, targetId, xValue));
                }
                continue;
            }
            int xValue = 0;
            if (hasX) {
                xValue = calculateSmartX(gd, card, null, virtualPool);
                if (xValue <= 0) continue;
            }
            castable.add(new SimulationAction.PlayCard(i, null, xValue));
        }
        return castable;
    }

    /**
     * Taps mana sources and plays the card from hand, paying any non-mana
     * additional costs (sacrifice / graveyard exile) heuristically.
     */
    private void executePlayCard(GameData gd, Player player, SimulationAction.PlayCard pc) {
        UUID playerId = player.getId();
        Card card = gd.playerHands.get(playerId).get(pc.handIndex());
        tapLandsForCard(gd, playerId, card, pc.xValue());
        List<Integer> exileIndices = computeExileNGraveyardIndices(gd, playerId, card);
        UUID sacrificeId = computeSacrificeTarget(gd, playerId, card);
        Integer discardIndex = computeDiscardCostIndex(gd, playerId, card);
        if (exileIndices != null || sacrificeId != null || discardIndex != null) {
            gameService.playCard(gd, player, pc.handIndex(), pc.xValue(), pc.targetId(),
                    null, List.of(), List.of(), false, sacrificeId, null, null, null, exileIndices,
                    false, discardIndex);
        } else {
            gameService.playCard(gd, player, pc.handIndex(), pc.xValue(), pc.targetId(), null);
        }
    }

    /**
     * Returns true when the simulated opponent may take a greedy sorcery-speed
     * action: its own main phase with an empty stack and nothing pending.
     */
    private boolean isOpponentSorceryWindow(GameData gd, UUID opponentId) {
        return opponentId.equals(gd.activePlayerId)
                && (gd.currentStep == TurnStep.PRECOMBAT_MAIN || gd.currentStep == TurnStep.POSTCOMBAT_MAIN)
                && gd.stack.isEmpty()
                && gd.interaction.activeInteraction() == null;
    }

    /**
     * Greedy opponent policy: play the first land in hand if the land drop is
     * still available. Returns true if a land was played.
     */
    private boolean tryOpponentPlayLand(GameData gd, Player opponent) {
        UUID opponentId = opponent.getId();
        try {
            if (gd.landsPlayedThisTurn.getOrDefault(opponentId, 0) != 0) return false;
            List<Card> hand = gd.playerHands.get(opponentId);
            if (hand == null) return false;
            for (int i = 0; i < hand.size(); i++) {
                Card card = hand.get(i);
                if (!card.hasType(CardType.LAND)) continue;
                synchronized (gd) {
                    gameService.playCard(gd, opponent, i, 0, null, null);
                }
                return handNoLongerContains(gd, opponentId, card);
            }
        } catch (Exception e) {
            log.trace("Simulated opponent land play failed: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Greedy opponent policy: cast the biggest affordable untargeted creature.
     * Returns true if a spell was put on the stack.
     * <p>
     * Deliberately much narrower and cheaper than {@link #enumerateCastableSpells}:
     * the production MCTS only completes a few dozen iterations per decision, so the
     * opponent policy cannot afford the full isCardPlayable legality pass, and
     * targeted spells (removal, auras) fired at the MCTS player's board would make
     * rollout rewards swing on the determinized hand instead of the MCTS player's
     * own decisions. Steady creature pressure is what fixes the frozen-opponent
     * over-optimism.
     */
    private boolean tryOpponentCastSpell(GameData gd, Player opponent) {
        UUID opponentId = opponent.getId();
        try {
            List<Card> hand = gd.playerHands.get(opponentId);
            if (hand == null) return false;
            ManaPool virtualPool = manaManager.buildVirtualManaPool(gd, opponentId);
            int bestIndex = -1;
            Card bestCard = null;
            for (int i = 0; i < hand.size(); i++) {
                Card card = hand.get(i);
                if (!card.hasType(CardType.CREATURE)) continue;
                if (card.getManaCost() == null) continue;
                if (card.isRequiresCreatureMana()) continue;
                if (EffectResolution.needsTarget(card) || card.isAura()) continue;
                ManaCost cost = new ManaCost(card.getManaCost());
                if (cost.hasX()) continue;
                if (!cost.canPay(virtualPool)) continue;
                if (!castingCostService.canPayAdditionalSpellCosts(gd, opponentId, card)) continue;
                if (bestCard == null || card.getManaValue() > bestCard.getManaValue()) {
                    bestIndex = i;
                    bestCard = card;
                }
            }
            if (bestCard == null) return false;
            synchronized (gd) {
                executePlayCard(gd, opponent, new SimulationAction.PlayCard(bestIndex, null, 0));
            }
            return handNoLongerContains(gd, opponentId, bestCard);
        } catch (Exception e) {
            log.trace("Simulated opponent spell cast failed: {}", e.getMessage());
            return false;
        }
    }

    private boolean handNoLongerContains(GameData gd, UUID playerId, Card card) {
        List<Card> hand = gd.playerHands.get(playerId);
        if (hand == null) return true;
        for (Card c : hand) {
            if (c == card) return false;
        }
        return true;
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
            // Kinds with a real heuristic (which card is worth least, keyword priorities,
            // punisher declines, damage-assignment math, ordering picks) keep their policy.
            case PendingInteraction.HandCardChoice cc -> resolveHandCardChoice(gd, player, cc);
            case PendingInteraction.DiscardChoice cc -> resolveHandCardChoice(gd, player, cc);
            case PendingInteraction.ColorChoice ccCtx -> {
                if (ccCtx.context() instanceof ChoiceContext.KeywordGrantChoice kgc) {
                    gameService.handleInteractionAnswer(gd, player, new InteractionAnswer.ListChoiceMade(kgc.options().getFirst().name()));
                } else if (ccCtx.context() instanceof ChoiceContext.StorageMatrixUntapChoice) {
                    gameService.handleInteractionAnswer(gd, player, new InteractionAnswer.ListChoiceMade("LAND"));
                } else if (ccCtx.context() instanceof ChoiceContext.ChooseModeChoice) {
                    gameService.handleInteractionAnswer(gd, player, new InteractionAnswer.ListChoiceMade(ccCtx.options().getFirst()));
                } else {
                    gameService.handleInteractionAnswer(gd, player, new InteractionAnswer.ListChoiceMade("RED"));
                }
            }
            case PendingInteraction.CombatDamageAssignment cda -> {
                Map<UUID, Integer> assignments = CombatDamageAssignmentHeuristic.assign(
                        cda, gd, gameQueryService);
                gameService.handleCombatDamageAssigned(gd, player, cda.attackerIndex(), assignments);
            }
            case PendingInteraction.Scry sc -> {
                if (sc.cards() != null) {
                    List<Integer> topOrder = new ArrayList<>();
                    for (int k = 0; k < sc.cards().size(); k++) topOrder.add(k);
                    gameService.handleInteractionAnswer(gd, player, new InteractionAnswer.ScryOrder(topOrder, List.of()));
                }
            }
            case PendingInteraction.LibraryReorder lr -> {
                if (lr.cards() != null) {
                    List<Integer> order = new ArrayList<>();
                    for (int k = 0; k < lr.cards().size(); k++) order.add(k);
                    gameService.handleInteractionAnswer(gd, player, new InteractionAnswer.CardOrder(order));
                }
            }
            case PendingInteraction.HandTopBottomChoice ignored -> gameService.handleInteractionAnswer(gd, player, new InteractionAnswer.HandTopBottom(0, 1));
            case PendingInteraction.LibraryRevealChoice lrc -> {
                if (lrc.validCardIds() != null && !lrc.validCardIds().isEmpty()) {
                    if (lrc.lifeCostPerSelection() > 0) {
                        // Punisher reveal (e.g. Sword-Point Diplomacy): deny nothing (don't pay life)
                        gameService.handleInteractionAnswer(gd, player, new InteractionAnswer.CardsChosen(List.of()));
                    } else {
                        // Normal library reveal: choose all valid cards
                        gameService.handleInteractionAnswer(gd, player, new InteractionAnswer.CardsChosen(new ArrayList<>(lrc.validCardIds())));
                    }
                }
            }
            // Everything else answers mechanically from the record's legal options
            // (first index / first permanent / up-to-max cards / minimum number / accept).
            default -> resolveWithOptions(gd, player, awaiting.legalOptions());
        }
    }

    /**
     * Maps an interaction's legal options onto candidate simulation actions. Only the shapes
     * with an existing {@link SimulationAction} wire mapping are enumerated (single index/ID
     * picks, list picks capped at {@link #MAX_LIST_OPTIONS}, accept/decline); the multi-pick,
     * numeric, and combinatorial shapes stay rollout-policy decisions (the caller falls back
     * to PassPriority when nothing is added here).
     */
    private static void addOptionsActions(List<SimulationAction> actions, InteractionOptions options) {
        switch (options) {
            case InteractionOptions.CardIndexPick p -> {
                if (p.validIndices() != null) {
                    for (int idx : p.validIndices()) {
                        actions.add(new SimulationAction.ChooseCard(idx));
                    }
                }
            }
            case InteractionOptions.PermanentPick p -> {
                for (UUID id : p.validIds()) {
                    actions.add(new SimulationAction.ChoosePermanent(id));
                }
            }
            case InteractionOptions.ListPick p -> {
                for (String option : p.options().stream().limit(MAX_LIST_OPTIONS).toList()) {
                    actions.add(new SimulationAction.ChooseColor(option));
                }
            }
            case InteractionOptions.AcceptDecline ignored -> {
                actions.add(new SimulationAction.MayAbilityChoice(true));
                actions.add(new SimulationAction.MayAbilityChoice(false));
            }
            case null, default -> {
            }
        }
    }

    /**
     * Rollout fallback: answers an interaction mechanically from its legal options — the
     * first legal index/ID, up to {@code maxCount} cards/permanents, the first list option,
     * the minimum number, or accept. Kinds needing a smarter pick keep explicit cases in
     * {@link #resolveInteraction}; unenumerated (combinatorial) shapes are skipped.
     */
    private void resolveWithOptions(GameData gd, Player player, InteractionOptions options) {
        switch (options) {
            case InteractionOptions.CardIndexPick p -> {
                if (p.validIndices() != null && !p.validIndices().isEmpty()) {
                    gameService.handleInteractionAnswer(gd, player, new InteractionAnswer.CardIndexChosen(p.validIndices().getFirst()));
                }
            }
            case InteractionOptions.GraveyardIndexPick p -> {
                if (p.validIndices() != null && !p.validIndices().isEmpty()) {
                    gameService.handleInteractionAnswer(gd, player, new InteractionAnswer.GraveyardCardChosen(p.validIndices().getFirst()));
                }
            }
            case InteractionOptions.LibraryIndexPick p -> {
                if (p.cardCount() > 0) {
                    gameService.handleInteractionAnswer(gd, player, new InteractionAnswer.LibraryCardChosen(0));
                }
            }
            case InteractionOptions.PermanentPick p -> {
                if (!p.validIds().isEmpty()) {
                    gameService.handleInteractionAnswer(gd, player, new InteractionAnswer.PermanentChosen(p.validIds().getFirst()));
                }
            }
            case InteractionOptions.MultiCardPick p -> {
                if (p.validCardIds() != null) {
                    gameService.handleInteractionAnswer(gd, player, new InteractionAnswer.CardsChosen(
                            p.validCardIds().stream().limit(p.maxCount()).toList()));
                }
            }
            case InteractionOptions.MultiPermanentPick p -> {
                if (p.validIds() != null) {
                    gameService.handleInteractionAnswer(gd, player, new InteractionAnswer.PermanentsChosen(
                            p.validIds().stream().limit(p.maxCount()).toList()));
                }
            }
            case InteractionOptions.ListPick p -> {
                if (!p.options().isEmpty()) {
                    gameService.handleInteractionAnswer(gd, player, new InteractionAnswer.ListChoiceMade(p.options().getFirst()));
                }
            }
            case InteractionOptions.NumberPick p -> gameService.handleInteractionAnswer(gd, player, new InteractionAnswer.NumberChosen(p.min()));
            case InteractionOptions.AcceptDecline ignored -> gameService.handleInteractionAnswer(gd, player, new InteractionAnswer.MayAbilityChosen(true));
            case null, default -> {
                // Unenumerated (ordering/combat) or unknown — skip
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
            gameService.handleInteractionAnswer(gd, player, new InteractionAnswer.CardIndexChosen(bestIdx));
        }
    }

    private UUID getInteractionPlayer(GameData gd) {
        // The active interaction record carries the decider
        PendingInteraction active = gd.interaction.activeInteraction();
        return active != null ? active.decidingPlayerId() : null;
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

    /**
     * Content-based equality for blocker assignment lists. {@code List.equals} on
     * {@code List<int[]>} compares the arrays by identity and never matches, so both
     * sides are compared in the canonical sorted (blockerIdx, attackerIdx) order that
     * {@code MCTSEngine.canonicalString} uses for {@code DeclareBlockers}.
     */
    private static boolean sameAssignments(List<int[]> a, List<int[]> b) {
        if (a.size() != b.size()) return false;
        Comparator<int[]> order = Comparator.<int[]>comparingInt(pair -> pair[0])
                .thenComparingInt(pair -> pair[1]);
        List<int[]> sortedA = new ArrayList<>(a);
        List<int[]> sortedB = new ArrayList<>(b);
        sortedA.sort(order);
        sortedB.sort(order);
        for (int i = 0; i < sortedA.size(); i++) {
            if (sortedA.get(i)[0] != sortedB.get(i)[0] || sortedA.get(i)[1] != sortedB.get(i)[1]) {
                return false;
            }
        }
        return true;
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
     * Computes graveyard card indices to exile for a cost that exiles an exact number of
     * graveyard cards of a given type. Returns null if the card has no such cost.
     */
    private List<Integer> computeExileNGraveyardIndices(GameData gd, UUID playerId, Card card) {
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            if (effect instanceof CostEffect cost && cost.consumedGraveyardCardCount() > 0) {
                int count = cost.consumedGraveyardCardCount();
                CardType requiredType = cost.consumedGraveyardCardType();
                List<Card> graveyard = gd.playerGraveyards.getOrDefault(playerId, List.of());
                List<Integer> matchingIndices = new ArrayList<>();
                for (int i = 0; i < graveyard.size(); i++) {
                    Card c = graveyard.get(i);
                    if (requiredType == null || c.hasType(requiredType)) {
                        matchingIndices.add(i);
                    }
                }
                if (matchingIndices.size() < count) return null;
                return new ArrayList<>(matchingIndices.subList(0, count));
            }
        }
        return null;
    }

    /**
     * Finds the first valid hand card to pay the card's "discard a card" additional cast cost.
     * Returns null if the card has no such cost (or, defensively, no valid discard exists —
     * enumeration already filters unpayable casts via canPayAdditionalSpellCosts).
     */
    private Integer computeDiscardCostIndex(GameData gd, UUID playerId, Card card) {
        List<Integer> valid = castingCostService.validDiscardCostIndices(gd, playerId, card);
        return valid == null || valid.isEmpty() ? null : valid.get(0);
    }

    /**
     * Finds the first valid sacrifice target for the card's sacrifice cost.
     * Returns null if the card has no sacrifice cost.
     */
    private UUID computeSacrificeTarget(GameData gd, UUID playerId, Card card) {
        List<Permanent> battlefield = gd.playerBattlefields.getOrDefault(playerId, List.of());
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            if (effect instanceof CostEffect cost) {
                PermanentPredicate filter = cost.consumedPermanentFilter();
                if (filter != null) {
                    return battlefield.stream()
                            .filter(p -> predicateEvaluationService.matchesPermanentPredicate(gd, p, filter))
                            .findFirst().map(Permanent::getId).orElse(null);
                }
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
                .anyMatch(e -> e instanceof ManaProducingEffect mp && mp.modeledByManaEstimator());
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
            if (effect instanceof ManaProducingEffect mp) {
                ManaColor color = mp.estimatedManaColor();
                if (color != null) {
                    int needed = coloredCosts.getOrDefault(color, 0);
                    int have = pool.get(color);
                    if (needed > have) return hasSideEffects ? 15 : 20;
                    return hasSideEffects ? 1 : 5;
                }
                if (mp.estimatedCountsAllColors()) return hasSideEffects ? 1 : 5;
            }
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
        return findCandidateTargets(gd, card, playerId, 1).stream().findFirst().orElse(null);
    }

    /**
     * Ranked, distinct target candidates for a spell or aura, best first, capped at
     * {@code maxCandidates}. Player-only and graveyard targeting stay single-candidate
     * to limit root branching; creature targeting offers the top two plus (for
     * "any target" spells) the opponent's face.
     */
    private List<UUID> findCandidateTargets(GameData gd, Card card, UUID playerId, int maxCandidates) {
        UUID opponentId = getOpponentId(gd, playerId);

        // Handle player-only targeting (e.g. Haunting Echoes, Mind Rot)
        Set<TargetType> allowedTargets = EffectResolution.computeAllowedTargets(card);
        if (allowedTargets.contains(TargetType.PLAYER) && !allowedTargets.contains(TargetType.PERMANENT)) {
            return List.of(opponentId);
        }

        // Handle graveyard targeting (e.g. Unburial Rites, Gruesome Encore)
        if (allowedTargets.contains(TargetType.GRAVEYARD)) {
            UUID graveyardTarget = findBestGraveyardTarget(gd, card, playerId, opponentId);
            return graveyardTarget == null ? List.of() : List.of(graveyardTarget);
        }

        // Handle auras — beneficial auras target own creatures, detrimental target opponent's
        if (card.isAura()) {
            // Controller-beneficial land auras (mana ramp like Wild Growth / Fertile Ground) help
            // whoever controls the enchanted land — enchant one of the AI's own lands, preferring
            // untapped ones so the extra mana is usable this turn.
            if (isControllerBeneficialLandAura(card)) {
                return gd.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                        .filter(p -> passesTargetFilter(gd, card, p, playerId))
                        .sorted(Comparator.comparing(Permanent::isTapped))
                        .limit(Math.min(2, maxCandidates))
                        .map(Permanent::getId)
                        .toList();
            }
            boolean isBeneficial = false;
            for (CardEffect effect : card.getEffects(EffectSlot.STATIC)) {
                if ((effect instanceof StaticCreatureBoostEffect boost
                        && (boost.scope() == GrantScope.ENCHANTED_CREATURE || boost.scope() == GrantScope.EQUIPPED_CREATURE))
                        || (effect instanceof KeywordGrantingEffect grant && grant.scope() == GrantScope.ENCHANTED_CREATURE)) {
                    isBeneficial = true;
                    break;
                }
            }
            if (isBeneficial) {
                return gd.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                        .filter(p -> gameQueryService.isCreature(gd, p))
                        .filter(p -> passesTargetFilter(gd, card, p, playerId))
                        .sorted(Comparator.comparingInt(
                                (Permanent p) -> gameQueryService.getEffectiveToughness(gd, p)).reversed())
                        .limit(Math.min(2, maxCandidates))
                        .map(Permanent::getId)
                        .toList();
            }
            // Detrimental aura — fall through to opponent's battlefield targeting below
        }

        // Beneficial non-aura spells (pumps, keyword grants, untaps, …) normally aim at the
        // caster's own board. Exception: pumping an undersized opponent creature so a
        // size-gated removal in hand / on board (Smite the Monstrous, Intrepid Hero, …)
        // becomes legal — MCTS must be able to explore that line.
        TargetPolarity polarity = polarityClassifier.classifyCard(gd, card, playerId);
        if (polarity == TargetPolarity.BENEFICIAL) {
            List<UUID> candidates = new ArrayList<>();
            gd.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                    .filter(p -> passesTargetFilter(gd, card, p, playerId))
                    .sorted(Comparator.comparingInt((Permanent p) ->
                            gameQueryService.getEffectivePower(gd, p)
                                    + gameQueryService.getEffectiveToughness(gd, p)).reversed())
                    .limit(Math.min(2, maxCandidates))
                    .map(Permanent::getId)
                    .forEach(candidates::add);

            if (candidates.size() < maxCandidates) {
                SizeGatedRemovalPump.findEnabledOpponentCreatures(
                                gd, card, playerId, opponentId, gameQueryService,
                                amountEvaluationService)
                        .stream()
                        .filter(p -> passesTargetFilter(gd, card, p, playerId))
                        .sorted(Comparator.comparingInt((Permanent p) ->
                                gameQueryService.getEffectivePower(gd, p)).reversed())
                        .map(Permanent::getId)
                        .filter(id -> !candidates.contains(id))
                        .limit((long) maxCandidates - candidates.size())
                        .forEach(candidates::add);
            }
            return candidates;
        }

        List<Permanent> oppBattlefield = gd.playerBattlefields.getOrDefault(opponentId, List.of());
        List<UUID> candidates = new ArrayList<>();

        // Prefer creatures that pass the target filter, biggest power first
        oppBattlefield.stream()
                .filter(p -> gameQueryService.isCreature(gd, p))
                .filter(p -> passesTargetFilter(gd, card, p, playerId))
                .sorted(Comparator.comparingInt(
                        (Permanent p) -> gameQueryService.getEffectivePower(gd, p)).reversed())
                .limit(Math.min(2, maxCandidates))
                .map(Permanent::getId)
                .forEach(candidates::add);

        // "Any target" spells (creature/planeswalker/player) can also go to the opponent's
        // face. The permanent fallback below must stay out of reach for them: it would pick
        // illegal targets like lands (a null target filter passes everything).
        if (allowedTargets.contains(TargetType.PLAYER) && candidates.size() < maxCandidates) {
            candidates.add(opponentId);
        }

        // Fall back to any permanent that passes the target filter (e.g., artifacts/enchantments for Naturalize)
        if (candidates.isEmpty()) {
            oppBattlefield.stream()
                    .filter(p -> passesTargetFilter(gd, card, p, playerId))
                    .findFirst()
                    .map(Permanent::getId)
                    .ifPresent(candidates::add);
        }
        return candidates;
    }

    /**
     * True for an aura whose only benefit accrues to the controller of the enchanted land — a
     * mana-ramp land aura like Wild Growth, Fertile Ground, or Overgrowth (an
     * {@link AddManaOnEnchantedLandTapEffect} in the land-tap slot). Such auras must be attached
     * to one of the AI's own lands; enchanting the opponent's land would ramp the opponent.
     */
    private static boolean isControllerBeneficialLandAura(Card card) {
        return card.isAura() && card.getEffects(EffectSlot.ON_ANY_PLAYER_TAPS_LAND).stream()
                .anyMatch(AddManaOnEnchantedLandTapEffect.class::isInstance);
    }

    private UUID findBestGraveyardTarget(GameData gd, Card card, UUID playerId, UUID opponentId) {
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            if (!effect.targetSpec().category().isGraveyard()) continue;

            List<Card> candidates;
            if (effect instanceof ReturnCardFromGraveyardEffect rge) {
                candidates = getSimGraveyardCandidates(gd, rge.source(), playerId, opponentId);
                if (rge.filter() != null) {
                    candidates = candidates.stream()
                            .filter(c -> predicateEvaluationService.matchesCardPredicate(c, rge.filter(), card.getId()))
                            .toList();
                }
            } else {
                GraveyardSearchScope scope = effect.targetSpec().category() == TargetCategory.ANY_GRAVEYARD_CARD
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

    private UUID getOpponentId(GameData gd, UUID playerId) {
        for (UUID id : gd.orderedPlayerIds) {
            if (!id.equals(playerId)) return id;
        }
        return null;
    }
}
