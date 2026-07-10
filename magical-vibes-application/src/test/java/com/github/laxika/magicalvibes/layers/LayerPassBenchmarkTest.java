package com.github.laxika.magicalvibes.layers;

import com.github.laxika.magicalvibes.cards.b.BloodMoon;
import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.d.Dub;
import com.github.laxika.magicalvibes.cards.e.ElvishChampion;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GoblinKing;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.ImperiousPerfect;
import com.github.laxika.magicalvibes.cards.l.Lignify;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MarchOfTheMachines;
import com.github.laxika.magicalvibes.cards.m.Maro;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.n.Nightmare;
import com.github.laxika.magicalvibes.cards.p.PaladinEnVec;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Micro-benchmark for the CR 613 layered whole-board computation (see
 * {@code agent-docs/LAYER_SYSTEM.md}, "Board cache"). The AI (MCTS simulation,
 * BoardEvaluator, CombatSimulator) hammers {@code GameQueryService} queries, each of which is
 * one external {@code computeStaticBonus} call; this measures full-board query sweeps over a
 * static-effect-heavy board, both steady-state (no mutation between sweeps — the board cache's
 * best case) and with one tap-state mutation per sweep (every sweep recomputes the board).
 *
 * <p>Disabled by default; run with {@code -DlayerBench=true}. Numbers are recorded in the
 * LAYER_SYSTEM.md Progress Log — absolute values are machine-dependent (and the test JVM runs
 * with {@code -XX:TieredStopAtLevel=1}), only before/after ratios on the same machine matter.
 */
@EnabledIfSystemProperty(named = "layerBench", matches = "true")
class LayerPassBenchmarkTest extends BaseCardTest {

    private static final long WARMUP_NANOS = 2_000_000_000L;
    private static final long MEASURED_NANOS = 4_000_000_000L;
    private static final int ROUNDS = 3;

    @Test
    void benchmarkFullBoardQuerySweeps() {
        List<Permanent> all = buildStaticHeavyBoard();
        int queriesPerSweep = all.size() * 4;

        runFor(all, false, WARMUP_NANOS);
        runFor(all, true, WARMUP_NANOS);

        System.out.printf("Board: %d permanents, %d queries/sweep, cache %s%n",
                all.size(), queriesPerSweep,
                Boolean.getBoolean("disableLayerBoardCache") ? "DISABLED" : "enabled");
        double bestSteady = 0;
        double bestMutating = 0;
        for (int round = 1; round <= ROUNDS; round++) {
            double steady = runFor(all, false, MEASURED_NANOS);
            double mutating = runFor(all, true, MEASURED_NANOS);
            bestSteady = Math.max(bestSteady, steady);
            bestMutating = Math.max(bestMutating, mutating);
            System.out.printf("Round %d: steady-state %.1f sweeps/s (%.0f queries/s), "
                            + "mutating %.1f sweeps/s (%.0f queries/s)%n",
                    round, steady, steady * queriesPerSweep, mutating, mutating * queriesPerSweep);
        }
        System.out.printf("BEST: steady-state %.1f sweeps/s (%.0f queries/s), "
                        + "mutating %.1f sweeps/s (%.0f queries/s)%n",
                bestSteady, bestSteady * queriesPerSweep, bestMutating, bestMutating * queriesPerSweep);
    }

    /** Runs sweeps for (at least) the given wall-clock window and returns sweeps/second. */
    private double runFor(List<Permanent> all, boolean mutateBetweenSweeps, long windowNanos) {
        Permanent toggled = all.get(0);
        long start = System.nanoTime();
        long elapsed;
        int sweeps = 0;
        do {
            if (mutateBetweenSweeps) {
                if (toggled.isTapped()) {
                    toggled.untap();
                } else {
                    toggled.tap();
                }
            }
            sweep(all);
            sweeps++;
            elapsed = System.nanoTime() - start;
        } while (elapsed < windowNanos);
        return sweeps / (elapsed / 1_000_000_000.0);
    }

    /** One full-board sweep, mimicking a BoardEvaluator pass: four layered queries per permanent. */
    private long sweep(List<Permanent> all) {
        long sink = 0;
        for (Permanent p : all) {
            sink += gqs.getEffectivePower(gd, p);
            sink += gqs.getEffectiveToughness(gd, p);
            sink += gqs.hasKeyword(gd, p, Keyword.FLYING) ? 1 : 0;
            sink += gqs.isCreature(gd, p) ? 1 : 0;
        }
        return sink;
    }

    private List<Permanent> buildStaticHeavyBoard() {
        List<Permanent> all = new ArrayList<>();
        all.add(add(player1, new GloriousAnthem()));
        all.add(add(player1, new GoblinKing()));
        all.add(add(player1, new ElvishChampion()));
        all.add(add(player1, new ImperiousPerfect()));
        all.add(add(player1, new LlanowarElves()));
        all.add(add(player1, new LlanowarElves()));
        all.add(add(player1, new LlanowarElves()));
        all.add(add(player1, new RagingGoblin()));
        Permanent bear1 = add(player1, new GrizzlyBears());
        Permanent bear2 = add(player1, new GrizzlyBears());
        all.add(bear1);
        all.add(bear2);
        all.add(add(player1, new Forest()));
        all.add(add(player1, new Forest()));
        all.add(add(player1, new Mountain()));

        all.add(add(player2, new BloodMoon()));
        all.add(add(player2, new MarchOfTheMachines()));
        all.add(add(player2, new Maro()));
        all.add(add(player2, new Nightmare()));
        all.add(add(player2, new PaladinEnVec()));
        all.add(add(player2, new FountainOfYouth()));
        all.add(add(player2, new CoralMerfolk()));
        all.add(add(player2, new Swamp()));
        all.add(add(player2, new Swamp()));

        all.add(attach(player1, new Dub(), bear1));
        all.add(attach(player2, new Lignify(), bear2));
        return all;
    }

    private Permanent add(Player player, Card card) {
        card.setOwnerId(player.getId());
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent attach(Player controller, Card attachment, Permanent target) {
        attachment.setOwnerId(controller.getId());
        Permanent perm = new Permanent(attachment);
        perm.setAttachedTo(target.getId());
        gd.playerBattlefields.get(controller.getId()).add(perm);
        return perm;
    }
}
