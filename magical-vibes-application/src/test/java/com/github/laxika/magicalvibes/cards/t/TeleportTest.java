package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TeleportTest extends BaseCardTest {

    @Test
    @DisplayName("During the declare attackers step, Teleport makes a target creature unblockable")
    void makesTargetUnblockableDuringDeclareAttackers() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Teleport()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Teleport cannot be cast outside the declare attackers step")
    void cannotCastOutsideDeclareAttackers() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Teleport()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Teleport's unblockable effect wears off at end of turn")
    void unblockableWearsOffAtEndOfTurn() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Teleport()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.isCantBeBlocked()).isFalse();
    }
}
