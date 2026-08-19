package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GontisMachinationsTest extends BaseCardTest {

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Gets energy the first time its controller loses life each turn")
    void getsEnergyOnFirstLifeLossEachTurn() {
        harness.addToBattlefield(player1, new GontisMachinations());
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Triggers again on the first life loss of a later turn")
    void getsEnergyAgainOnLaterTurn() {
        harness.addToBattlefield(player1, new GontisMachinations());
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        advanceTurn();
        advanceTurn();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("Pays two energy and sacrifices itself to drain each opponent")
    void paysEnergyAndSacrificesToDrainOpponents() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new GontisMachinations());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        gd.playerEnergyCounters.put(player1.getId(), 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
        harness.assertNotOnBattlefield(player1, "Gonti's Machinations");
        harness.assertInGraveyard(player1, "Gonti's Machinations");
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
    }

    @Test
    @DisplayName("Cannot activate without two energy counters")
    void cannotActivateWithoutTwoEnergyCounters() {
        Permanent machinations = harness.addToBattlefieldAndReturn(player1, new GontisMachinations());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two energy counters");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(machinations);
    }
}
