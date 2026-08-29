package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BloodClockTest extends BaseCardTest {

    @Test
    @DisplayName("The active player may pay 2 life to keep their permanents")
    void activePlayerMayPayLife() {
        harness.addToBattlefield(player1, new BloodClock());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("If the active player declines, they choose a permanent they control to return")
    void decliningPaymentReturnsChosenPermanent() {
        harness.addToBattlefield(player1, new BloodClock());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(forest.getId());
        harness.handlePermanentChosen(player2, forest.getId());

        harness.assertInHand(player2, "Forest");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Blood Clock");
    }

    @Test
    @DisplayName("Declining with no permanent to return does nothing")
    void decliningWithNoPermanentDoesNothing() {
        harness.addToBattlefield(player1, new BloodClock());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Blood Clock");
    }
}
