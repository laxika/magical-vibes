package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoblinLyreTest extends BaseCardTest {

    @Test
    @DisplayName("Winning damages the opponent, losing damages you, by the respective creature counts")
    void flipDamagesOneSideByCreatureCount() {
        harness.addToBattlefield(player1, new GoblinLyre());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        int ownLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Sacrifice is a cost, so the Lyre is gone either way.
        harness.assertNotOnBattlefield(player1, "Goblin Lyre");
        harness.assertInGraveyard(player1, "Goblin Lyre");

        boolean won = gd.gameLog.stream().map(GameLogEntry::plainText)
                .anyMatch(log -> log.contains("wins the coin flip for Goblin Lyre"));

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

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(ownLifeBefore);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    @DisplayName("The controller cannot be chosen as the target")
    void cannotTargetSelf() {
        harness.addToBattlefield(player1, new GoblinLyre());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .hasMessageContaining("opponent");
    }
}
