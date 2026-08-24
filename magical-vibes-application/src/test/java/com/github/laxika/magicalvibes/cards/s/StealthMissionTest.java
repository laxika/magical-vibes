package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StealthMission.class, GrizzlyBears.class, Forest.class})
class StealthMissionTest extends BaseCardTest {

    @Test
    @DisplayName("Puts two +1/+1 counters on the target and makes it unblockable")
    void putsCountersAndMakesTargetUnblockable() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castStealthMission(bears.getId());

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bears.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("The unblockable effect wears off at end of turn, but the counters remain")
    void unblockableWearsOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castStealthMission(bears.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.isCantBeBlocked()).isFalse();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Only targets creatures you control")
    void rejectsIllegalTargets() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        assertThatThrownBy(() -> castStealthMission(opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addToBattlefield(player1, new Forest());
        UUID forestId = harness.getPermanentId(player1, "Forest");
        assertThatThrownBy(() -> castStealthMission(forestId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castStealthMission(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new StealthMission()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
