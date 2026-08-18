package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RavingOniSlaveTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield makes its controller lose 3 life without a Demon")
    void enteringWithoutDemonCausesLifeLoss() {
        harness.setHand(player1, List.of(new RavingOniSlave()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Entering the battlefield causes no life loss while its controller controls a Demon")
    void enteringWithDemonCausesNoLifeLoss() {
        harness.addToBattlefield(player1, new RenegadeDemon());
        harness.setHand(player1, List.of(new RavingOniSlave()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Leaving the battlefield makes its controller lose 3 life without a Demon")
    void leavingWithoutDemonCausesLifeLoss() {
        Permanent slave = harness.addToBattlefieldAndReturn(player1, new RavingOniSlave());
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, slave));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Leaving the battlefield causes no life loss while its controller controls a Demon")
    void leavingWithDemonCausesNoLifeLoss() {
        Permanent slave = harness.addToBattlefieldAndReturn(player1, new RavingOniSlave());
        harness.addToBattlefield(player1, new RenegadeDemon());
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, slave));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }
}
