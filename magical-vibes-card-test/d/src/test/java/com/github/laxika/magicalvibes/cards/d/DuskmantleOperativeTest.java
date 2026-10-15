package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DuskmantleOperative.class, AirElemental.class, GrizzlyBears.class})
class DuskmantleOperativeTest extends BaseCardTest {

    @Test
    @DisplayName("Can't be blocked by a creature with power 4")
    void cannotBeBlockedByCreatureWithPowerFour() {
        Permanent blocker = addCreatureReady(player2, new AirElemental());
        Permanent operative = addCreatureReady(player1, new DuskmantleOperative());
        prepareBlockers(operative);

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(operative);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be blocked by a creature with power less than 4")
    void canBeBlockedByCreatureWithPowerLessThanFour() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent operative = addCreatureReady(player1, new DuskmantleOperative());
        prepareBlockers(operative);

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(operative);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 5})
    @DisplayName("Uses increased current power when rejecting blockers")
    void cannotBeBlockedWhenCurrentPowerIsAtLeastFour(int power) {
        Permanent blocker = addCreatureReady(player2, new DuskmantleOperative());
        blocker.setPowerModifier(power - 2);
        Permanent operative = addCreatureReady(player1, new DuskmantleOperative());
        prepareBlockers(operative);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be blocked by a creature whose current power is exactly 3")
    void canBeBlockedWhenCurrentPowerIsThree() {
        Permanent blocker = addCreatureReady(player2, new DuskmantleOperative());
        blocker.setPowerModifier(1);
        Permanent operative = addCreatureReady(player1, new DuskmantleOperative());
        prepareBlockers(operative);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Can be blocked by a printed power 4 creature reduced to power 3")
    void canBeBlockedWhenPrintedPowerFourIsReduced() {
        Permanent blocker = addCreatureReady(player2, new AirElemental());
        blocker.setPowerModifier(-1);
        Permanent operative = addCreatureReady(player1, new DuskmantleOperative());
        prepareBlockers(operative);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private void prepareBlockers(Permanent operative) {
        operative.setAttacking(true);
        prepareDeclareBlockers();
    }
}
