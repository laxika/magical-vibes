package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FemerefScouts;
import com.github.laxika.magicalvibes.cards.s.SewerRats;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MtendaHerder.class, FemerefScouts.class, SewerRats.class})
class MtendaHerderTest extends BaseCardTest {

    @Test
    @DisplayName("Flanking gives a blocker without flanking -1/-1 until end of turn")
    void blockerWithoutFlankingGetsMinusOneMinusOne() {
        Permanent herder = addCreatureReady(player1, new MtendaHerder());
        herder.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new FemerefScouts());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(blocker.getId());
        assertThat(entry.isNonTargeting()).isTrue();

        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isZero();
        assertThat(blocker.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("A 1/1 blocker without flanking dies to the flanking trigger")
    void oneOneBlockerDies() {
        Permanent herder = addCreatureReady(player1, new MtendaHerder());
        herder.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SewerRats());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(blocker.getId()));
    }

    @Test
    @DisplayName("Each non-flanking blocker gets its own flanking trigger")
    void eachNonFlankingBlockerGetsItsOwnTrigger() {
        Permanent herder = addCreatureReady(player1, new MtendaHerder());
        herder.setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new FemerefScouts());
        Permanent secondBlocker = addCreatureReady(player2, new FemerefScouts());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(firstBlocker.getEffectivePower()).isZero();
        assertThat(firstBlocker.getEffectiveToughness()).isEqualTo(3);
        assertThat(secondBlocker.getEffectivePower()).isZero();
        assertThat(secondBlocker.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("The flanking penalty expires at end of turn")
    void flankingPenaltyExpiresAtEndOfTurn() {
        Permanent herder = addCreatureReady(player1, new MtendaHerder());
        herder.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new FemerefScouts());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isZero();
        assertThat(blocker.getEffectiveToughness()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(1);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("A blocker that also has flanking is unaffected")
    void blockerWithFlankingIsUnaffected() {
        Permanent herder = addCreatureReady(player1, new MtendaHerder());
        herder.setAttacking(true);
        addCreatureReady(player2, new MtendaHerder());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An unblocked creature with flanking creates no trigger")
    void unblockedCreatesNoTrigger() {
        Permanent herder = addCreatureReady(player1, new MtendaHerder());
        herder.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
    }

}
