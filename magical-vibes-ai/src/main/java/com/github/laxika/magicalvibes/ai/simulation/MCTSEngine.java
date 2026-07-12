package com.github.laxika.magicalvibes.ai.simulation;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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
    /**
     * Default per-decision think time, overridable via the
     * {@code ai.mcts.time-budget-ms} application property. 2500ms buys roughly
     * 90–140 iterations per decision. The simulated opponent playing lands and
     * creatures during rollouts (GameSimulator's greedy policy) makes each
     * iteration costlier and narrows root-action reward gaps, so the old 1200ms
     * (~30–60 iterations) no longer separated good casts from passes.
     */
    public static final long DEFAULT_TIME_BUDGET_MS = 2500;

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

    /**
     * Early stopping: minimum total root visits (including warm-start prior visits)
     * before the convergence check may fire. Below this the tree has no statistical
     * basis and the search always runs its full budget. Deliberately high: fresh
     * searches on heavy boards complete far fewer iterations than this, so in practice
     * only warm-started trees (visits accumulated across repeated decision points) and
     * fast/light searches qualify — thin-tree truncation under CPU load was measured to
     * flip marginal casts into passes.
     */
    private static final int EARLY_STOP_MIN_ROOT_VISITS = 100;

    /**
     * Early stopping: safety multiplier applied to the time-based estimate of how many
     * iterations still fit in the remaining budget. Overestimating the remaining
     * iterations makes the check strictly more conservative (stops less often), which
     * protects against a slow outlier iteration skewing the per-iteration average.
     */
    private static final int EARLY_STOP_TIME_SAFETY_FACTOR = 2;

    /**
     * Directly constructed engines default to a single-threaded search. The production
     * app opts into parallelism through {@code AiPlayerService} (see
     * {@link #autoParallelism()} and the {@code ai.mcts.parallelism} property) — but the
     * test suite constructs engines directly and runs many forked JVMs concurrently, and
     * parallel workers on top of that oversubscription was measured to destabilize
     * wall-clock-budgeted searches and timing-asserting tests across the whole suite.
     */
    public static final int DEFAULT_PARALLELISM = 1;

    /**
     * Worker count for parallel time-budgeted searches when the {@code ai.mcts.parallelism}
     * property asks for auto-sizing: half the available cores. Each MCTS iteration works on
     * its own determinized {@code GameData} copy, so workers only meet at the shared tree
     * (guarded by {@link #treeLock}); iteration cost is milliseconds while tree operations
     * are microseconds, so contention is negligible and throughput scales near-linearly.
     * The other half of the cores is left for the live game, other AI games, and the web
     * layer; set {@code ai.mcts.parallelism} explicitly to override.
     */
    public static int autoParallelism() {
        return Math.max(1, Runtime.getRuntime().availableProcessors() / 2);
    }

    /**
     * Shared daemon pool for parallel search workers, sized on demand and shared by
     * all engine instances (one HardAiDecisionEngine per game). Idle threads expire,
     * so a quiet server holds no MCTS threads.
     */
    private static final ExecutorService SEARCH_POOL = Executors.newCachedThreadPool(runnable -> {
        Thread thread = new Thread(runnable, "mcts-search-worker");
        thread.setDaemon(true);
        return thread;
    });

    private final GameSimulator simulator;
    private final Determinizer determinizer;
    private final Random rng;
    private final boolean timeBudgetEnabled;
    private final int maxBudget;
    private long timeBudgetMs = DEFAULT_TIME_BUDGET_MS;

    /**
     * Guards all reads/writes of the shared search tree (node visits, rewards,
     * children, untried actions). Never held while simulating.
     */
    private final Object treeLock = new Object();

    /**
     * Worker count for time-budgeted searches. Deterministic (seeded) engines always
     * search single-threaded regardless of this value — thread scheduling would break
     * reproducibility.
     */
    private int parallelism = DEFAULT_PARALLELISM;

    /**
     * Warm-start cache. When the AI passes priority and then gets priority back at a
     * structurally identical decision point (same legal actions), we can reuse the
     * previous search tree to keep accumulating visit statistics. This is especially
     * valuable when an opponent pass or a stack resolution leaves us at the same
     * information set — all the tree work done in the prior call is still valid.
     * <p>
     * Invalidated automatically whenever the root legal-action signature changes.
     */
    private MCTSNode cachedRoot;
    private String cachedSignature;
    private int cacheHits;
    private int cacheMisses;

    // Diagnostics for the most recent search() call (benchmarking / logging).
    private int lastSearchIterations;
    private int lastSearchFailures;
    private long lastSearchElapsedMs;
    private boolean lastSearchEarlyStopped;

    public MCTSEngine(GameSimulator simulator) {
        this.simulator = simulator;
        this.determinizer = new Determinizer();
        this.rng = new Random();
        this.timeBudgetEnabled = true;
        this.maxBudget = 0; // 0 = no cap, use caller's budget
        SimulationLogSuppressor.install();
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
        SimulationLogSuppressor.install();
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
        // Flag this thread as simulating so SimulationLogSuppressor mutes engine
        // logging for every rollout action (parallel workers flag themselves).
        SimulationLogSuppressor.enterSimulation();
        try {
            return doSearch(rootState, aiPlayerId, budget);
        } finally {
            SimulationLogSuppressor.exitSimulation();
        }
    }

    private SimulationAction doSearch(GameData rootState, UUID aiPlayerId, int budget) {
        lastSearchIterations = 0;
        lastSearchFailures = 0;
        lastSearchElapsedMs = 0;
        lastSearchEarlyStopped = false;
        List<SimulationAction> rootActions = simulator.getLegalActions(rootState, aiPlayerId);
        if (rootActions.isEmpty()) {
            return new SimulationAction.PassPriority();
        }
        if (rootActions.size() == 1) {
            return rootActions.getFirst();
        }

        // Warm-start from the cached tree if the decision point hasn't changed.
        // Otherwise invalidate and build a fresh root.
        String signature = buildSignature(rootActions);
        MCTSNode root;
        if (cachedRoot != null && signature.equals(cachedSignature)) {
            root = cachedRoot;
            cacheHits++;
            if (log.isDebugEnabled()) {
                log.debug("MCTS: Warm-starting from cached tree ({} prior visits, {} children expanded)",
                        root.visits, root.children.size());
            }
        } else {
            root = new MCTSNode(null, null, rootActions);
            cachedRoot = root;
            cachedSignature = signature;
            cacheMisses++;
        }
        long searchStart = System.currentTimeMillis();
        long deadline = timeBudgetEnabled ? searchStart + timeBudgetMs : Long.MAX_VALUE;
        int effectiveBudget = maxBudget > 0 ? Math.min(budget, maxBudget) : budget;

        if (timeBudgetEnabled && parallelism > 1) {
            searchParallel(rootState, aiPlayerId, root, searchStart, deadline, effectiveBudget);
        } else {
            searchSequential(rootState, aiPlayerId, root, searchStart, deadline, effectiveBudget);
        }
        lastSearchElapsedMs = System.currentTimeMillis() - searchStart;

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
     * Single-threaded search loop: used by deterministic (seeded) engines, where thread
     * scheduling would break reproducibility, and when parallelism is configured to 1.
     */
    private void searchSequential(GameData rootState, UUID aiPlayerId, MCTSNode root,
                                  long searchStart, long deadline, int effectiveBudget) {
        for (int i = 0; i < effectiveBudget; i++) {
            if (timeBudgetEnabled && System.currentTimeMillis() > deadline) {
                log.debug("MCTS: Time budget exceeded after {} simulations", i);
                break;
            }
            lastSearchIterations = i + 1;

            try {
                runIteration(rootState, aiPlayerId, root, deadline, rng);
            } catch (Exception e) {
                lastSearchFailures++;
                log.trace("MCTS simulation {} failed: {}", i, e.getMessage());
            }

            if (isDecided(root, i + 1, searchStart, deadline, effectiveBudget)) {
                lastSearchEarlyStopped = true;
                log.debug("MCTS: Early stop after {} iterations — best action cannot be overtaken", i + 1);
                break;
            }
        }
    }

    /**
     * Root-parallel search loop over the shared tree: {@link #parallelism} workers run
     * {@link #runIteration} concurrently, each on its own determinized state with its own
     * RNG, meeting only at the tree lock. Workers stop at the deadline, when the shared
     * iteration budget is exhausted, or when the convergence check settles the decision.
     */
    private void searchParallel(GameData rootState, UUID aiPlayerId, MCTSNode root,
                                long searchStart, long deadline, int effectiveBudget) {
        AtomicInteger started = new AtomicInteger();
        AtomicInteger completed = new AtomicInteger();
        AtomicInteger failures = new AtomicInteger();
        AtomicBoolean decided = new AtomicBoolean();

        List<Future<?>> workers = new ArrayList<>(parallelism);
        for (int w = 0; w < parallelism; w++) {
            Random workerRng = new Random(rng.nextLong());
            workers.add(SEARCH_POOL.submit(() -> {
                // Pooled threads may carry a stale interrupt flag from a cancelled search
                Thread.interrupted();
                SimulationLogSuppressor.enterSimulation();
                try {
                    while (!decided.get()
                            && System.currentTimeMillis() <= deadline
                            && started.incrementAndGet() <= effectiveBudget) {
                        try {
                            runIteration(rootState, aiPlayerId, root, deadline, workerRng);
                        } catch (Exception e) {
                            failures.incrementAndGet();
                            log.trace("MCTS simulation failed: {}", e.getMessage());
                        }
                        int done = completed.incrementAndGet();
                        synchronized (treeLock) {
                            if (isDecided(root, done, searchStart, deadline, effectiveBudget)) {
                                decided.set(true);
                            }
                        }
                    }
                } finally {
                    SimulationLogSuppressor.exitSimulation();
                }
            }));
        }

        for (Future<?> worker : workers) {
            long waitMs = Math.max(1_000, deadline - System.currentTimeMillis() + 10_000);
            try {
                worker.get(waitMs, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                worker.cancel(true);
                log.warn("MCTS: search worker exceeded the time budget and was cancelled");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                worker.cancel(true);
                return;
            } catch (ExecutionException e) {
                log.warn("MCTS: search worker failed", e.getCause());
            }
        }

        lastSearchIterations = completed.get();
        lastSearchFailures = failures.get();
        lastSearchEarlyStopped = decided.get();
    }

    /**
     * One MCTS iteration: determinize → select → replay → expand → rollout → backpropagate.
     * Tree reads and writes happen under {@link #treeLock}; the expensive phases
     * (determinization, path replay, expansion legality pass, rollout) run outside it, so
     * parallel workers spend almost no time contending.
     * <p>
     * Expansion uses reserve-then-commit: the untried action is removed under the lock
     * (so no other worker expands it), applied outside the lock, and the child committed
     * under the lock. If the iteration fails or the replay ends in a terminal state, the
     * reservation is restored at its original position so the action is not lost and the
     * sequential expansion order stays identical to the pre-parallel implementation.
     */
    private void runIteration(GameData rootState, UUID aiPlayerId, MCTSNode root,
                              long deadline, Random iterationRng) {
        // 1. DETERMINIZE: Create a plausible complete-information state
        GameData simState = determinizer.determinize(rootState, aiPlayerId, iterationRng);

        // 2. SELECT: Traverse tree using UCB1, reserving one untried action for expansion
        MCTSNode node;
        SimulationAction reserved = null;
        synchronized (treeLock) {
            node = select(root);
            if (!node.untriedActions.isEmpty()) {
                reserved = node.untriedActions.removeFirst();
            }
        }

        boolean expanded = false;
        try {
            // 3. REPLAY: Apply all actions along the tree path to synchronize
            //    the determinized state with the selected node's position.
            //    Without this, deeper tree nodes would evaluate from the wrong state.
            boolean terminal = false;
            for (SimulationAction pathAction : node.pathFromRoot()) {
                simulator.applyAction(simState, aiPlayerId, pathAction);
                if (simulator.isTerminal(simState)) {
                    terminal = true;
                    break;
                }
            }

            // 4. EXPAND: Apply the reserved action and commit the child node
            if (reserved != null && !terminal) {
                simulator.applyAction(simState, aiPlayerId, reserved);
                List<SimulationAction> childActions = simulator.isTerminal(simState)
                        ? List.of()
                        : simulator.getLegalActions(simState, aiPlayerId);
                synchronized (treeLock) {
                    node = node.addExpandedChild(reserved, childActions);
                }
                expanded = true;
            }

            // 5. ROLLOUT: Play out using softmax/epsilon-greedy heuristic policy
            double reward = rollout(simState, aiPlayerId, node, deadline, iterationRng);

            // 6. BACKPROPAGATE: Update visit counts and rewards.
            //    Deadline-truncated rollouts (NaN) are discarded — their reward is
            //    biased against the just-cast spell (see rollout javadoc).
            if (!Double.isNaN(reward)) {
                synchronized (treeLock) {
                    backpropagate(node, reward);
                }
            }
        } finally {
            if (reserved != null && !expanded) {
                synchronized (treeLock) {
                    node.untriedActions.add(0, reserved);
                }
            }
        }
    }

    /**
     * Early-stopping convergence check. The final action is the most visited root child,
     * so once no other root child (existing or yet to be expanded) can catch up to the
     * leader within the iterations that can still possibly run, the search outcome is
     * fixed and the remaining budget would be wasted.
     * <p>
     * The bound on remaining iterations is exact for the iteration budget and a
     * conservative ({@link #EARLY_STOP_TIME_SAFETY_FACTOR}× overestimated) projection for
     * the wall-clock budget, so this stops strictly after the decision is settled — it
     * never changes which action {@link #search} returns. Warm-started trees carry their
     * prior visit lead, which is what lets a repeat search at an already-converged
     * decision point return almost immediately.
     */
    private boolean isDecided(MCTSNode root, int iterationsDone, long searchStart,
                              long deadline, int effectiveBudget) {
        if (root.visits < EARLY_STOP_MIN_ROOT_VISITS || root.children.isEmpty()) {
            return false;
        }

        int bestVisits = 0;
        int secondVisits = 0;
        for (MCTSNode child : root.children) {
            if (child.visits > bestVisits) {
                secondVisits = bestVisits;
                bestVisits = child.visits;
            } else if (child.visits > secondVisits) {
                secondVisits = child.visits;
            }
        }

        long remainingIterations = effectiveBudget - iterationsDone;
        if (timeBudgetEnabled) {
            long now = System.currentTimeMillis();
            long remainingMs = deadline - now;
            if (remainingMs <= 0) {
                return false; // deadline check at the top of the loop handles this
            }
            long avgIterationMs = Math.max(1, (now - searchStart) / iterationsDone);
            long timeBasedEstimate = EARLY_STOP_TIME_SAFETY_FACTOR * (remainingMs / avgIterationMs);
            remainingIterations = Math.min(remainingIterations, timeBasedEstimate);
        }

        return bestVisits - secondVisits > remainingIterations;
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
     * ROLLOUT phase: From the current state, play out using softmax/epsilon-greedy
     * heuristic policy for a limited number of moves, then evaluate.
     *
     * @return the normalized state evaluation, or {@link Double#NaN} when the deadline
     *         cut the rollout short. A truncated rollout often evaluates a state where
     *         the cast spell still sits unresolved on the stack (mana and card spent,
     *         no payoff yet), which systematically scores casting worse than passing —
     *         so the caller must discard the reward instead of backpropagating it.
     */
    private double rollout(GameData simState, UUID aiPlayerId, MCTSNode node, long deadline, Random rolloutRng) {
        for (int depth = 0; depth < DEFAULT_ROLLOUT_DEPTH; depth++) {
            if (timeBudgetEnabled && System.currentTimeMillis() > deadline) return Double.NaN;
            if (simulator.isTerminal(simState)) break;

            List<SimulationAction> actions = simulator.getLegalActions(simState, aiPlayerId);
            if (actions.isEmpty()) break;

            SimulationAction action = selectRolloutAction(simState, actions, aiPlayerId, rolloutRng);
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

    /**
     * Overrides the per-decision think time. Only meaningful for time-budgeted
     * engines; deterministic test engines ignore it.
     */
    public void setTimeBudgetMs(long timeBudgetMs) {
        this.timeBudgetMs = timeBudgetMs;
    }

    /**
     * Overrides the parallel worker count (see {@code ai.mcts.parallelism}). Only
     * meaningful for time-budgeted engines; deterministic test engines always run
     * single-threaded. A value of 1 forces the sequential loop.
     */
    public void setParallelism(int parallelism) {
        this.parallelism = Math.max(1, parallelism);
    }

    /**
     * Clears the warm-start cache. The next {@link #search} call will build a
     * fresh tree regardless of legal-action signature match.
     */
    public void clearCache() {
        cachedRoot = null;
        cachedSignature = null;
    }

    /** Iterations attempted by the most recent {@link #search} call. Exposed for benchmarks/diagnostics. */
    public int getLastSearchIterations() {
        return lastSearchIterations;
    }

    /** Iterations of the most recent {@link #search} call that ended in a swallowed exception. */
    public int getLastSearchFailures() {
        return lastSearchFailures;
    }

    /** Wall-clock milliseconds the most recent {@link #search} call spent iterating. */
    public long getLastSearchElapsedMs() {
        return lastSearchElapsedMs;
    }

    /** Whether the most recent {@link #search} call ended via the early-stopping convergence check. */
    public boolean isLastSearchEarlyStopped() {
        return lastSearchEarlyStopped;
    }

    /** Number of times {@link #search} reused the cached tree. Exposed for tests/diagnostics. */
    public int getCacheHits() {
        return cacheHits;
    }

    /** Number of times {@link #search} built a fresh tree (first call or invalidation). */
    public int getCacheMisses() {
        return cacheMisses;
    }

    /**
     * Returns the sum of visit counts across the cached root's children — i.e. the
     * total number of MCTS iterations that have flowed through the current warm-start
     * tree. Useful for tests asserting that the tree accumulates visits across calls.
     */
    public int getCachedRootChildVisitSum() {
        if (cachedRoot == null) return 0;
        int sum = 0;
        for (MCTSNode child : cachedRoot.children) {
            sum += child.visits;
        }
        return sum;
    }

    /**
     * Builds a stable signature from the root legal-action list. Two states produce the
     * same signature iff they present the same set of root decisions to the AI — which
     * is the strongest soundness guarantee we can get without introspecting {@link GameData}.
     */
    private static String buildSignature(List<SimulationAction> actions) {
        List<String> parts = new ArrayList<>(actions.size());
        for (SimulationAction a : actions) {
            parts.add(canonicalString(a));
        }
        Collections.sort(parts);
        return String.join("|", parts);
    }

    /**
     * Produces a canonical string form for a simulation action. Records that contain
     * primitive arrays (notably {@link SimulationAction.DeclareBlockers}) cannot rely on
     * the default {@code toString} for stable comparison, so they get explicit handling.
     */
    private static String canonicalString(SimulationAction action) {
        if (action instanceof SimulationAction.DeclareBlockers db) {
            List<int[]> sorted = new ArrayList<>(db.blockerAssignments());
            sorted.sort(Comparator.<int[]>comparingInt(a -> a[0]).thenComparingInt(a -> a[1]));
            StringBuilder sb = new StringBuilder("DB[");
            for (int i = 0; i < sorted.size(); i++) {
                if (i > 0) sb.append(',');
                sb.append(sorted.get(i)[0]).append("->").append(sorted.get(i)[1]);
            }
            return sb.append(']').toString();
        }
        if (action instanceof SimulationAction.DeclareAttackers da) {
            List<Integer> sorted = new ArrayList<>(da.attackerIndices());
            Collections.sort(sorted);
            return "DA" + sorted;
        }
        return action.toString();
    }
}
