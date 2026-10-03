package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.k.KnightOfSursi;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BogardanLancer.class, BlindPhantasm.class, KnightOfSursi.class})
class BogardanLancerTest extends BaseCardTest {

    @Test
    @DisplayName("Bloodthirst 1 enters with a +1/+1 counter after an opponent was dealt damage")
    void bloodthirstApplies() {
        gd.recordDamageToPlayer(player2.getId(), 1);
        castLancer();

        assertThat(findPermanent(player1, "Bogardan Lancer")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bloodthirst 1 does not apply when no opponent was dealt damage")
    void bloodthirstDoesNotApply() {
        castLancer();

        assertThat(findPermanent(player1, "Bogardan Lancer")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bloodthirst 1 ignores damage dealt to the Lancer's controller")
    void bloodthirstIgnoresControllerDamage() {
        gd.recordDamageToPlayer(player1.getId(), 1);
        castLancer();

        assertThat(findPermanent(player1, "Bogardan Lancer")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Flanking gives a non-flanking blocker -1/-1 until end of turn")
    void flankingDebuffsNonFlankingBlocker() {
        addCreatureReady(player1, new BogardanLancer());
        Permanent blocker = addCreatureReady(player2, new BlindPhantasm());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(3);
    }

    @Test
    @DisplayName("Flanking does not affect a blocker that also has flanking")
    void flankingDoesNotDebuffFlankingBlocker() {
        addCreatureReady(player1, new BogardanLancer());
        Permanent blocker = addCreatureReady(player2, new KnightOfSursi());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(2);
    }

    @Test
    @DisplayName("An unblocked creature with flanking creates no trigger")
    void unblockedCreatesNoFlankingTrigger() {
        addCreatureReady(player1, new BogardanLancer());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Bloodthirst grants only one counter even after several damage events")
    void bloodthirstDoesNotScaleWithDamage() {
        gd.recordDamageToPlayer(player2.getId(), 3);
        gd.recordDamageToPlayer(player2.getId(), 4);
        castLancer();

        assertThat(findPermanent(player1, "Bogardan Lancer")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Bloodthirst applies when entering without being cast and creates no trigger")
    void bloodthirstAppliesWithoutCasting() {
        gd.recordDamageToPlayer(player2.getId(), 1);

        Permanent lancer = harness.enterBattlefieldAndReturn(player1, new BogardanLancer());

        assertThat(lancer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flanking debuffs each non-flanking blocker separately")
    void flankingDebuffsEachBlocker() {
        addCreatureReady(player1, new BogardanLancer());
        Permanent firstBlocker = addCreatureReady(player2, new BlindPhantasm());
        Permanent secondBlocker = addCreatureReady(player2, new BlindPhantasm());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, firstBlocker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, firstBlocker)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, secondBlocker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, secondBlocker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Flanking does not trigger when the Lancer blocks")
    void flankingDoesNotTriggerOnBlocking() {
        addCreatureReady(player2, new BlindPhantasm());
        addCreatureReady(player1, new BogardanLancer());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
    }

    private void castLancer() {
        harness.castFromHand(player1, new BogardanLancer(), "{1}{R}");
        resolveAllTriggers();
    }
}
