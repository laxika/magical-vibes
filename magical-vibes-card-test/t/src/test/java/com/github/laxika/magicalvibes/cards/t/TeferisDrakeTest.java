package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AzimaetDrake;
import com.github.laxika.magicalvibes.cards.j.JungleWurm;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TeferisDrake.class, JungleWurm.class, AzimaetDrake.class})
class TeferisDrakeTest extends BaseCardTest {

    @Test
    void phasesOutBeforeUntappingAndReturnsOnlyOnControllersNextUntap() {
        Permanent drake = addCreatureReady(player1, new TeferisDrake());
        drake.tap();
        drake.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.performUntapStep(player2);
        harness.assertOnBattlefield(player1, "Teferi's Drake");
        assertThat(drake.isTapped()).isTrue();

        harness.performUntapStep(player1);
        harness.assertNotOnBattlefield(player1, "Teferi's Drake");
        assertThat(gd.phasedOutPermanents.get(player1.getId())).containsExactly(drake);
        assertThat(drake.isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Teferi's Drake");

        harness.performUntapStep(player2);
        harness.assertNotOnBattlefield(player1, "Teferi's Drake");
        assertThat(gd.phasedOutPermanents.get(player1.getId())).containsExactly(drake);

        harness.performUntapStep(player1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(drake);
        assertThat(gd.phasedOutPermanents.getOrDefault(player1.getId(), List.of())).doesNotContain(drake);
        assertThat(drake.isTapped()).isFalse();
        assertThat(drake.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.performUntapStep(player1);
        harness.assertNotOnBattlefield(player1, "Teferi's Drake");
    }

    @Test
    void flyingPreventsGroundCreatureFromBlocking() {
        addCreatureReady(player1, new TeferisDrake());
        addCreatureReady(player2, new JungleWurm());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void flyingCreatureCanBlock() {
        addCreatureReady(player1, new TeferisDrake());
        Permanent blocker = addCreatureReady(player2, new AzimaetDrake());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
