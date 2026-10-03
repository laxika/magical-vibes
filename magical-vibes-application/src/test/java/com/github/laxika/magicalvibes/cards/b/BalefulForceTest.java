package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BalefulForce.class})
class BalefulForceTest extends BaseCardTest {

    @Test
    @DisplayName("Your upkeep draws a card and loses 1 life")
    void yourUpkeepDrawsAndLosesLife() {
        harness.addToBattlefield(player1, new BalefulForce());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new BalefulForce()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("An opponent's upkeep still makes you draw and lose 1 life")
    void opponentUpkeepAffectsController() {
        harness.addToBattlefield(player1, new BalefulForce());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        BalefulForce drawn = new BalefulForce();
        harness.setLibrary(player1, List.of(drawn));
        harness.setLibrary(player2, List.of(new BalefulForce()));
        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(drawn.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLifeBefore - 1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    @DisplayName("Each Force contributes a separate upkeep trigger")
    void multipleForcesEachDrawAndLoseLife() {
        harness.addToBattlefield(player1, new BalefulForce());
        harness.addToBattlefield(player1, new BalefulForce());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new BalefulForce(), new BalefulForce()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, lifeBefore - 1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, lifeBefore - 2);
    }

    @Test
    @DisplayName("A Force controlled by the opponent affects only that opponent")
    void opponentControlledForceAffectsItsController() {
        harness.addToBattlefield(player2, new BalefulForce());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new BalefulForce()));
        int playerLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertLife(player1, playerLifeBefore);
        harness.assertLife(player2, opponentLifeBefore - 1);
    }
}
