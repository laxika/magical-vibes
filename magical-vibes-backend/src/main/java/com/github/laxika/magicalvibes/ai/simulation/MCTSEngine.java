package com.github.laxika.magicalvibes.ai.simulation;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;

/**
 * Information Set Monte Carlo Tree Search (IS-MCTS) engine.
 *
 * Instead of evaluating a single "what happens if I play X" heuristic,
 * this explores hundreds of possible game trajectories by:
 * 1. Determinizing hidden information (opponent's hand/deck)
 * 2. Selecting promising nodes via UCB1
 * 3. Expanding new actions
 * 4. Rolling out with heuristic policy
 * 5. Backpropagating rewards
 *
 * The best action is the one with the most visits (most robust).
 */
@Slf4j
public class MCTSEngine {

    private static final double EXPLORATION_CONSTANT = 1.41; // √2
    private static final int DEFAULT_ROLLOUT_DEPTH = 20;
    private static final long TIME_BUDGET_MS = 1200;

    /**
     * Softmax temperature for rollout action selection.
     * Lower values → more greedy (exploit heuristic knowledge),
     * higher values → more uniform (explore diverse lines).
     * With typical spell values ranging 0–30, a temperature of 6.0 means:
     * - A 6-point advantage → ~2.7x more likely to be selected
     * - A 12-point advantage → ~7.4x more likely
     */
    private static final double ROLLOUT_TEMPERATURE = 6.0;

    /**
     * Epsilon for epsilon-greedy exploration during rollouts.
     * With this probability, a uniformly random action is chosen instead of
     * softmax sampling. Ensures even unusual plays get some exploration.
     */
    private static final double EPSILON = 0.05;

    private final GameSimulator simulator;
    private final Determinizer determinizer;
    private final Random rng;
    private final boolean timeBudgetEnabled;
    private final int maxBudget;

    public MCTSEngine(GameSimulator simulator) {
        this.simulator = simulator;
        this.determinizer = new Determinizer();
        this.rng = new Random();
        this.timeBudgetEnabled = true;
        this.maxBudget = 0; // 0 = no cap, use caller's budget
    }

    /**
     * Creates a deterministic MCTS engine for testing.
     * Uses a seeded Random for reproducible results and disables the time budget.
     *
     * @param seed      Random seed for reproducibility
     * @param maxBudget Maximum number of iterations (caps the caller's budget)
     */
    public MCTSEngine(GameSimulator simulator, long seed, int maxBudget) {
        this.simulator = simulator;
        this.determinizer = new Determinizer();
        this.rng = new Random(seed);
        this.timeBudgetEnabled = false;
        this.maxBudget = maxBudget;
    }

    /**
     * Runs IS-MCTS search and returns the best action.
     *
     * @param rootState   Current game state (not modified)
     * @param aiPlayerId  The AI player's ID
     * @param budget      Number of simulations to run
     * @return The best action to take
     */
    public SimulationAction search(GameData rootState, UUID aiPlayerId, int budget) {
        List<SimulationAction> rootActions = simulator.getLegalActions(rootState, aiPlayerId);
        if (rootActions.isEmpty()) {
            return new SimulationAction.PassPriority();
        }
        if (rootActions.size() == 1) {
            return rootActions.getFirst();
        }

        MCTSNode root = new MCTSNode(null, null, rootActions);
        long deadline = timeBudgetEnabled ? System.currentTimeMillis() + TIME_BUDGET_MS : Long.MAX_VALUE;
        int effectiveBudget = maxBudget > 0 ? Math.min(budget, maxBudget) : budget;

        for (int i = 0; i < effectiveBudget; i++) {
            if (timeBudgetEnabled && System.currentTimeMillis() > deadline) {
                log.debug("MCTS: Time budget exceeded after {} simulations", i);
                break;
            }

            try {
                // 1. DETERMINIZE: Create a plausible complete-information state
                GameData simState = determinizer.determinize(rootState, aiPlayerId, this.rng);

                // 2. SELECT: Traverse tree using UCB1
                MCTSNode node = select(root);

                // 3. REPLAY: Apply all actions along the tree path to synchronize
                //    the determinized state with the selected node's position.
                //    Without this, deeper tree nodes would evaluate from the wrong state.
                for (SimulationAction pathAction : node.pathFromRoot()) {
                    simulator.applyAction(simState, aiPlayerId, pathAction);
                    if (simulator.isTerminal(simState)) break;
                }

                // 4. EXPAND: If untried actions remain, expand one
                if (!node.untriedActions.isEmpty() && !simulator.isTerminal(simState)) {
                    node = expand(node, simState, aiPlayerId);
                }

                // 5. ROLLOUT: Play out using softmax/epsilon-greedy heuristic policy
                double reward = rollout(simState, aiPlayerId, node, deadline, this.rng);

                // 6. BACKPROPAGATE: Update visit counts and rewards
                backpropagate(node, reward);
            } catch (Exception e) {
                log.trace("MCTS simulation {} failed: {}", i, e.getMessage());
            }
        }

        // Return the most visited child's action
        MCTSNode bestChild = root.mostVisitedChild();
        if (bestChild == null) {
            return rootActions.getFirst();
        }

        if (log.isDebugEnabled()) {
            log.debug("MCTS: Best action {} (visits={}, avgReward={})",
                    bestChild.action, bestChild.visits,
                    bestChild.visits > 0 ? String.format("%.3f", bestChild.totalReward / bestChild.visits) : "0");
            for (MCTSNode child : root.children) {
                log.debug("  {} visits={} avg={}", child.action, child.visits,
                        child.visits > 0 ? String.format("%.3f", child.totalReward / child.visits) : "0");
            }
        }

        return bestChild.action;
    }

    /**
     * SELECT phase: Walk down the tree choosing children by UCB1 until we reach
     * a node with untried actions or a leaf.
     */
    private MCTSNode select(MCTSNode node) {
        while (node.isFullyExpanded() && !node.isLeaf()) {
            node = node.bestChild(EXPLORATION_CONSTANT);
            if (node == null) break;
        }
        return node != null ? node : new MCTSNode(null, null, List.of());
    }

    /**
     * EXPAND phase: Pick an untried action, apply it, create a child node.
     */
    private MCTSNode expand(MCTSNode node, GameData simState, UUID aiPlayerId) {
        SimulationAction action = node.untriedActions.getFirst();

        // Apply the action to the simulation state
        simulator.applyAction(simState, aiPlayerId, action);

        // Get legal actions for the new state
        List<SimulationAction> childActions;
        if (simulator.isTerminal(simState)) {
            childActions = List.of();
        } else {
            childActions = simulator.getLegalActions(simState, aiPlayerId);
        }

        return node.addChild(action, childActions);
    }

    /**
     * ROLLOUT phase: From the current state, play out using softmax/epsilon-greedy
     * heuristic policy for a limited number of moves, then evaluate.
     */
    private double rollout(GameData simState, UUID aiPlayerId, MCTSNode node, long deadline, Random rolloutRng) {
        for (int depth = 0; depth < DEFAULT_ROLLOUT_DEPTH; depth++) {
            if (timeBudgetEnabled && System.currentTimeMillis() > deadline) break;
            if (simulator.isTerminal(simState)) break;

            List<SimulationAction> actions = simulator.getLegalActions(simState, aiPlayerId);
            if (actions.isEmpty()) break;

            SimulationAction action = selectRolloutAction(simState, actions, aiPlayerId, rng);
            try {
                simulator.applyAction(simState, aiPlayerId, action);
            } catch (Exception e) {
                break;
            }
        }

        return simulator.evaluate(simState, aiPlayerId);
    }

    /**
     * Softmax/epsilon-greedy rollout policy.
     * <p>
     * With probability {@link #EPSILON}, picks a uniformly random action (pure exploration).
     * Otherwise, scores every legal action with domain heuristics and samples via
     * softmax-weighted distribution (controlled by {@link #ROLLOUT_TEMPERATURE}).
     * <p>
     * This is strictly better than the old greedy policy because it lets MCTS discover
     * non-obvious lines (e.g. "cast a weak cantrip now → draw removal → win")
     * while still strongly preferring high-value plays most of the time.
     */
    SimulationAction selectRolloutAction(GameData simState, List<SimulationAction> actions,
                                                  UUID aiPlayerId, Random rng) {
        if (actions.size() == 1) return actions.getFirst();

        // Epsilon-greedy: with small probability, pick a uniformly random action
        if (rng.nextDouble() < EPSILON) {
            return actions.get(rng.nextInt(actions.size()));
        }

        // Score each action using domain heuristics
        double[] scores = new double[actions.size()];
        for (int i = 0; i < actions.size(); i++) {
            scores[i] = scoreRolloutAction(simState, actions.get(i), aiPlayerId);
        }

        // Select via softmax-weighted sampling
        return softmaxSelect(actions, scores, rng);
    }

    /**
     * Scores a single action for the softmax rollout policy.
     * Higher scores → more likely to be selected.
     */
    double scoreRolloutAction(GameData simState, SimulationAction action, UUID aiPlayerId) {
        if (action instanceof SimulationAction.PlayCard pc) {
            List<Card> hand = simState.playerHands.get(aiPlayerId);
            if (hand != null && pc.handIndex() < hand.size()) {
                Card card = hand.get(pc.handIndex());
                double value = simulator.getSpellEvaluator().estimateSpellValue(simState, card, aiPlayerId);
                // Floor at 0.1 so even weak/situational spells have some chance of being explored
                return Math.max(value, 0.1);
            }
            return 0.1;
        }
        if (action instanceof SimulationAction.DeclareAttackers da) {
            return da.attackerIndices().isEmpty() ? 0.5 : 5.0;
        }
        if (action instanceof SimulationAction.DeclareBlockers db) {
            return db.blockerAssignments().isEmpty() ? 0.5 : 5.0;
        }
        if (action instanceof SimulationAction.ActivateAbility) {
            return 3.0;
        }
        if (action instanceof SimulationAction.MayAbilityChoice mac) {
            return mac.accept() ? 2.0 : 0.5;
        }
        if (action instanceof SimulationAction.PassPriority) {
            return 0.1;
        }
        // ChooseCard, ChoosePermanent, ChooseColor — neutral
        return 1.0;
    }

    /**
     * Samples an action from a softmax distribution over the given scores.
     * Uses the standard numerical stability trick of subtracting the max score
     * before exponentiating to avoid overflow.
     */
    SimulationAction softmaxSelect(List<SimulationAction> actions, double[] scores, Random rng) {
        double maxScore = IntStream.range(0, scores.length)
                .mapToDouble(i -> scores[i])
                .max()
                .orElse(0);

        double[] weights = new double[actions.size()];
        double totalWeight = 0;
        for (int i = 0; i < scores.length; i++) {
            weights[i] = Math.exp((scores[i] - maxScore) / ROLLOUT_TEMPERATURE);
            totalWeight += weights[i];
        }

        double roll = rng.nextDouble() * totalWeight;
        double cumulative = 0;
        for (int i = 0; i < weights.length; i++) {
            cumulative += weights[i];
            if (roll <= cumulative) {
                return actions.get(i);
            }
        }

        return actions.getLast();
    }

    /**
     * BACKPROPAGATE phase: Walk from the node back to root,
     * incrementing visits and adding reward.
     */
    private void backpropagate(MCTSNode node, double reward) {
        while (node != null) {
            node.visits++;
            node.totalReward += reward;
            node = node.parent;
        }
    }
}
