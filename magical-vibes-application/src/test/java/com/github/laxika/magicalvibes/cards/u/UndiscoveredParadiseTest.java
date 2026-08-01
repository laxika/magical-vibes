package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UndiscoveredParadiseTest extends BaseCardTest {

    @Test
    @DisplayName("Mana ability adds the chosen color and schedules the bounce")
    void manaAbilityAddsColorAndSchedulesBounce() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new UndiscoveredParadise());
        int before = gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(land.isReturnToHandAtNextUntap()).isTrue();

        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(before + 1);
        assertThat(land.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Undiscovered Paradise");
    }

    @Test
    @DisplayName("Returns to owner's hand during controller's next untap step")
    void returnsToHandAtNextUntap() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new UndiscoveredParadise());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");
        assertThat(land.isReturnToHandAtNextUntap()).isTrue();

        advanceTurn(); // player2's untap — still on battlefield
        harness.assertOnBattlefield(player1, "Undiscovered Paradise");

        advanceTurn(); // player1's untap — bounce

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Undiscovered Paradise"));
        harness.assertInHand(player1, "Undiscovered Paradise");
    }

    @Test
    @DisplayName("Without activating it stays on the battlefield through untap")
    void staysWithoutActivation() {
        harness.addToBattlefield(player1, new UndiscoveredParadise());

        advanceTurn();
        advanceTurn();

        harness.assertOnBattlefield(player1, "Undiscovered Paradise");
    }

    @Test
    @DisplayName("Returns even when skip-untap would keep it tapped")
    void returnsEvenWhenItWouldNotUntap() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new UndiscoveredParadise());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");
        land.setSkipUntapCount(1);

        advanceTurn();
        advanceTurn();

        harness.assertInHand(player1, "Undiscovered Paradise");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Undiscovered Paradise"));
    }

    @Test
    @DisplayName("Does not bounce during an opponent's untap step")
    void doesNotBounceOnOpponentUntap() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new UndiscoveredParadise());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        advanceTurn(); // player2's untap

        harness.assertOnBattlefield(player1, "Undiscovered Paradise");
        assertThat(land.isReturnToHandAtNextUntap()).isTrue();
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
