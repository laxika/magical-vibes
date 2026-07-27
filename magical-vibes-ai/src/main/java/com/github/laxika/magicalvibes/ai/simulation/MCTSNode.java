package com.github.laxika.magicalvibes.ai.simulation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

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

    /**
     * Reward-scale adjustment applied to this node's action at final selection, carrying what the
     * AI knows about it that the search's own reward signal is too coarse to see (see
     * {@link MCTSEngine#computeSelectionAdjustments}). Zero — the neutral value — whenever there
     * is nothing to add.
     */
    double selectionAdjustment = 0.0;

    /**
     * Selection adjustments for this node's children, keyed by {@link MCTSEngine#canonicalString}.
     * Set on the root only, before the search starts, and read-only afterwards — which is what
     * lets parallel workers adjust a child they expand without extra synchronization beyond the
     * tree lock.
     */
    Map<String, Double> childSelectionAdjustments;

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
     * Selects the child with the highest UCB1 value. Selection adjustments deliberately play no
     * part here: steering the tree by them only starves the demoted action of the visits its own
     * mean is estimated from, and final selection no longer reads visit counts anyway. Search
     * samples the alternatives evenly; {@link #bestRewardChild} is where they are applied.
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
     * Selects the child with the most visits. Used by the convergence check; final move
     * selection goes through {@link #bestRewardChild} instead.
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
     * Final move selection: the best mean reward, plus each child's selection adjustment, among
     * children visited often enough for that mean to carry weight — at least
     * {@code minVisitFraction} of the most-visited child.
     * <p>
     * Visit count alone does not survive this engine's reward scale. {@code evaluate} squashes a
     * board through a sigmoid into a band roughly 0.03 wide, so UCB1's exploration term at
     * {@code EXPLORATION_CONSTANT} dwarfs every reward difference and drives visit counts to
     * near-equal no matter which action is better; the most-visited child is then whichever one
     * happened to end a visit ahead. The means the search collected say more — and where even they
     * are noise, {@link #selectionAdjustment} carries what the AI knows independently.
     * <p>
     * The visit floor keeps the robustness that "most visits" was there for: a child the search
     * abandoned cannot win on a mean drawn from a handful of early rollouts.
     */
    MCTSNode bestRewardChild(double minVisitFraction) {
        int maxVisits = 0;
        for (int i = 0; i < children.size(); i++) {
            maxVisits = Math.max(maxVisits, children.get(i).visits);
        }
        if (maxVisits == 0) {
            return null;
        }
        int visitFloor = Math.max(1, (int) Math.ceil(maxVisits * minVisitFraction));

        MCTSNode best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < children.size(); i++) {
            MCTSNode child = children.get(i);
            if (child.visits < visitFloor) {
                continue;
            }
            double score = child.totalReward / child.visits + child.selectionAdjustment;
            if (score > bestScore) {
                bestScore = score;
                best = child;
            }
        }
        return best;
    }

    /**
     * Expands this node by creating a child for the given action. The action must
     * already have been reserved (removed from {@link #untriedActions}) by the caller —
     * the reserve-then-commit split lets the parallel search apply the action and
     * enumerate the child's legal moves outside the tree lock.
     */
    MCTSNode addExpandedChild(SimulationAction action, List<SimulationAction> childActions) {
        MCTSNode child = new MCTSNode(action, this, childActions);
        children.add(child);
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
