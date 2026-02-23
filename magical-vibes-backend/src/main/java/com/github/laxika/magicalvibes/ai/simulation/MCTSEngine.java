package com.github.laxika.magicalvibes.ai.simulation;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Random;
import java.util.UUID;

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
    private static final int DEFAULT_ROLLOUT_DEPTH = 6;
    private static final long TIME_BUDGET_MS = 900;

    private final GameSimulator simulator;
    private final Determinizer determinizer;

    public MCTSEngine(GameSimulator simulator) {
        this.simulator = simulator;
        this.determinizer = new Determinizer();
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
        Random rng = new Random();
        long deadline = System.currentTimeMillis() + TIME_BUDGET_MS;

        for (int i = 0; i < budget; i++) {
            if (System.currentTimeMillis() > deadline) {
                log.debug("MCTS: Time budget exceeded after {} simulations", i);
                break;
            }

            try {
                // 1. DETERMINIZE: Create a plausible complete-information state
                GameData simState = determinizer.determinize(rootState, aiPlayerId, rng);

                // 2. SELECT: Traverse tree using UCB1
                MCTSNode node = select(root);

                // 3. EXPAND: If untried actions remain, expand one
                if (!node.untriedActions.isEmpty() && !simulator.isTerminal(simState)) {
                    node = expand(node, simState, aiPlayerId);
                }

                // 4. ROLLOUT: Play out using heuristic policy
                double reward = rollout(simState, aiPlayerId, node);

                // 5. BACKPROPAGATE: Update visit counts and rewards
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
     * ROLLOUT phase: From the current state, play out using heuristic policy
     * for a limited number of moves, then evaluate.
     */
    private double rollout(GameData simState, UUID aiPlayerId, MCTSNode node) {
        // Apply remaining actions from root to this node
        // (In IS-MCTS with determinization, the state is already partially played out during expand)

        for (int depth = 0; depth < DEFAULT_ROLLOUT_DEPTH; depth++) {
            if (simulator.isTerminal(simState)) break;

            // Get legal actions and pick using heuristic
            List<SimulationAction> actions = simulator.getLegalActions(simState, aiPlayerId);
            if (actions.isEmpty()) break;

            SimulationAction action = selectRolloutAction(simState, actions, aiPlayerId);
            try {
                simulator.applyAction(simState, aiPlayerId, action);
            } catch (Exception e) {
                break;
            }
        }

        return simulator.evaluate(simState, aiPlayerId);
    }

    /**
     * Heuristic rollout policy — much stronger than random.
     * Uses SpellEvaluator and CombatSimulator to pick good actions.
     */
    private SimulationAction selectRolloutAction(GameData simState, List<SimulationAction> actions,
                                                  UUID aiPlayerId) {
        // If there's only one option, take it
        if (actions.size() == 1) return actions.getFirst();

        // For spell casting, pick highest value
        SimulationAction bestSpell = null;
        double bestSpellValue = Double.NEGATIVE_INFINITY;

        for (SimulationAction action : actions) {
            if (action instanceof SimulationAction.PlayCard pc) {
                List<Card> hand = simState.playerHands.get(aiPlayerId);
                if (hand != null && pc.handIndex() < hand.size()) {
                    Card card = hand.get(pc.handIndex());
                    double value = simulator.getSpellEvaluator().estimateSpellValue(simState, card, aiPlayerId);
                    if (value > bestSpellValue) {
                        bestSpellValue = value;
                        bestSpell = action;
                    }
                }
            }
        }

        if (bestSpell != null && bestSpellValue > 0) {
            return bestSpell;
        }

        // For attacker declarations, prefer the non-empty one (the CombatSimulator result)
        for (SimulationAction action : actions) {
            if (action instanceof SimulationAction.DeclareAttackers da && !da.attackerIndices().isEmpty()) {
                return action;
            }
        }

        // For blocker declarations, prefer the non-empty one
        for (SimulationAction action : actions) {
            if (action instanceof SimulationAction.DeclareBlockers db && !db.blockerAssignments().isEmpty()) {
                return action;
            }
        }

        // Default: pass priority or first available
        for (SimulationAction action : actions) {
            if (action instanceof SimulationAction.PassPriority) return action;
        }
        return actions.getFirst();
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
