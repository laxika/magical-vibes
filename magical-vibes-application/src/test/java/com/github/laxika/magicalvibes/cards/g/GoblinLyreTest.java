package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinLyre.class, BalduvianBears.class})
class GoblinLyreTest extends BaseCardTest {

    @Test
    @DisplayName("Winning damages the opponent, losing damages you, by the respective creature counts")
    void flipDamagesOneSideByCreatureCount() {
        harness.addToBattlefield(player1, new GoblinLyre());
        addCreatureReady(player1, new BalduvianBears());
        addCreatureReady(player1, new BalduvianBears());
        addCreatureReady(player1, new BalduvianBears());
        addCreatureReady(player2, new BalduvianBears());

        int ownLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Sacrifice is a cost, so the Lyre is gone either way.
        harness.assertNotOnBattlefield(player1, "Goblin Lyre");
        harness.assertInGraveyard(player1, "Goblin Lyre");

        boolean won = gameLogContains("wins the coin flip for Goblin Lyre");
        boolean lost = gameLogContains("loses the coin flip for Goblin Lyre");
        assertThat(won ^ lost).isTrue();

        if (won) {
            // Three creatures the controller controls.
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 3);
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(ownLifeBefore);
        } else {
            // One creature the targeted opponent controls.
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(ownLifeBefore - 1);
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
        }
    }

    @Test
    @DisplayName("Neither player takes damage when the relevant battlefield has no creatures")
    void noCreaturesMeansNoDamage() {
        harness.addToBattlefield(player1, new GoblinLyre());

        int ownLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gameLogContains("coin flip for Goblin Lyre")).isTrue();
        harness.assertNotOnBattlefield(player1, "Goblin Lyre");
        harness.assertInGraveyard(player1, "Goblin Lyre");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(ownLifeBefore);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    @CardUsed(JaceBeleren.class)
    @DisplayName("A planeswalker target uses its controller's creature count on a lost flip")
    void targetsPlaneswalkerAndUsesItsControllerForLoss() {
        harness.addToBattlefield(player1, new GoblinLyre());
        addCreatureReady(player1, new BalduvianBears());
        addCreatureReady(player1, new BalduvianBears());
        addCreatureReady(player2, new BalduvianBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);

        int ownLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());
        int loyaltyBefore = planeswalker.getCounterCount(CounterType.LOYALTY);

        harness.activateAbility(player1, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        boolean won = gameLogContains("wins the coin flip for Goblin Lyre");
        boolean lost = gameLogContains("loses the coin flip for Goblin Lyre");
        assertThat(won ^ lost).isTrue();
        harness.assertNotOnBattlefield(player1, "Goblin Lyre");
        harness.assertInGraveyard(player1, "Goblin Lyre");

        if (won) {
            assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(loyaltyBefore - 2);
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(ownLifeBefore);
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
        } else {
            assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(loyaltyBefore);
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(ownLifeBefore - 1);
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
        }
    }

    @Test
    @CardUsed(JaceBeleren.class)
    @DisplayName("A planeswalker controlled by the ability's controller is a legal target")
    void canTargetOwnPlaneswalker() {
        harness.addToBattlefield(player1, new GoblinLyre());
        addCreatureReady(player1, new BalduvianBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);

        int ownLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int loyaltyBefore = planeswalker.getCounterCount(CounterType.LOYALTY);

        harness.activateAbility(player1, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        boolean won = gameLogContains("wins the coin flip for Goblin Lyre");
        boolean lost = gameLogContains("loses the coin flip for Goblin Lyre");
        assertThat(won ^ lost).isTrue();
        harness.assertNotOnBattlefield(player1, "Goblin Lyre");
        harness.assertInGraveyard(player1, "Goblin Lyre");

        if (won) {
            assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(loyaltyBefore - 1);
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(ownLifeBefore);
        } else {
            assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(loyaltyBefore);
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(ownLifeBefore - 1);
        }
    }

    @Test
    @DisplayName("The controller cannot be chosen as the target")
    void cannotTargetSelf() {
        harness.addToBattlefield(player1, new GoblinLyre());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .hasMessageContaining("opponent");
        harness.assertOnBattlefield(player1, "Goblin Lyre");
        harness.assertNotInGraveyard(player1, "Goblin Lyre");
    }
}
