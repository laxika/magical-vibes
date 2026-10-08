package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.ArdentMilitia;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TolarianDrake.class, ArdentMilitia.class})
class TolarianDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Phases out during its controller's untap step and phases in during the next one")
    void phasesOutAndInDuringControllerUntapSteps() {
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new TolarianDrake());

        advanceToUpkeep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(drake);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(drake);

        advanceToUpkeep(player2);

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(drake);

        advanceToUpkeep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(drake);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).doesNotContain(drake);
    }

    @Test
    @DisplayName("Flying prevents a nonflying creature from blocking it")
    void flyingPreventsNonFlyingBlocker() {
        Permanent drake = addCreatureReady(player1, new TolarianDrake());
        Permanent blocker = addCreatureReady(player2, new ArdentMilitia());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(drake);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Phasing precedes untapping and preserves counters on the same permanent")
    void phasingPreservesCountersAndUntapsOnlyAfterPhasingIn() {
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new TolarianDrake());
        drake.tap();
        drake.getCounters().put(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.performUntapStep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(drake);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(drake);
        assertThat(drake.isTapped()).isTrue();
        assertThat(drake.getCounters()).containsEntry(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.assertNotInGraveyard(player1, "Tolarian Drake");

        harness.performUntapStep(player2);
        assertThat(drake.isTapped()).isTrue();

        harness.performUntapStep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(drake);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).doesNotContain(drake);
        assertThat(drake.isTapped()).isFalse();
        assertThat(drake.getCounters()).containsEntry(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.assertNotInGraveyard(player1, "Tolarian Drake");
    }

    @Test
    @DisplayName("A flying creature can block Tolarian Drake")
    void flyingCreatureCanBlock() {
        Permanent drake = addCreatureReady(player1, new TolarianDrake());
        Permanent blocker = addCreatureReady(player2, new TolarianDrake());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(drake);

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .doesNotThrowAnyException();
    }
}
