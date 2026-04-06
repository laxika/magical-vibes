package com.github.laxika.magicalvibes.ai.simulation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Node in the MCTS search tree.
 * Tracks visit counts and accumulated rewards for UCB1 selection.
 */
class MCTSNode {

    final SimulationAction action;        // action that led to this node (null for root)
    final MCTSNode parent;
    final List<MCTSNode> children = new ArrayList<>();
    final List<SimulationAction> untriedActions;  // actions not yet expanded
    int visits = 0;
    double totalReward = 0.0;

    MCTSNode(SimulationAction action, MCTSNode parent, List<SimulationAction> legalActions) {
        this.action = action;
        this.parent = parent;
        this.untriedActions = new ArrayList<>(legalActions);
    }

    /**
     * Upper Confidence Bound for Trees (UCB1) formula.
     * Balances exploitation (average reward) and exploration (visit count).
     */
    double ucb1(double explorationParam) {
        if (visits == 0) return Double.MAX_VALUE;
        return (totalReward / visits)
                + explorationParam * Math.sqrt(Math.log(parent.visits) / visits);
    }

    /**
     * Selects the child with the highest UCB1 value.
     */
    MCTSNode bestChild(double explorationParam) {
        MCTSNode best = null;
        double bestValue = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < children.size(); i++) {
            MCTSNode child = children.get(i);
            double value = child.ucb1(explorationParam);
            if (value > bestValue) {
                bestValue = value;
                best = child;
            }
        }
        return best;
    }

    /**
     * Selects the child with the most visits (used for final move selection).
     */
    MCTSNode mostVisitedChild() {
        MCTSNode best = null;
        int bestVisits = -1;
        for (int i = 0; i < children.size(); i++) {
            MCTSNode child = children.get(i);
            if (child.visits > bestVisits) {
                bestVisits = child.visits;
                best = child;
            }
        }
        return best;
    }

    /**
     * Expands this node by creating a child for the given action.
     */
    MCTSNode addChild(SimulationAction action, List<SimulationAction> childActions) {
        MCTSNode child = new MCTSNode(action, this, childActions);
        children.add(child);
        untriedActions.remove(action);
        return child;
    }

    boolean isFullyExpanded() {
        return untriedActions.isEmpty();
    }

    boolean isLeaf() {
        return children.isEmpty();
    }

    /**
     * Returns the sequence of actions from the root to this node.
     * Used by IS-MCTS to replay the tree path on each determinized state,
     * ensuring the simulation state is synchronized with the tree position.
     */
    List<SimulationAction> pathFromRoot() {
        Deque<SimulationAction> path = new ArrayDeque<>();
        MCTSNode node = this;
        while (node.parent != null) {
            path.addFirst(node.action);
            node = node.parent;
        }
        return new ArrayList<>(path);
    }
}
