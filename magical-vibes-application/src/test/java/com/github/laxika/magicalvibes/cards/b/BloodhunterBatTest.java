package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BloodhunterBatTest extends BaseCardTest {

    @Test
    @DisplayName("ETB trigger drains 2 life from the target opponent and gains 2 life")
    void etbDrainsTargetOpponent() {
        castBat(player2.getId());
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB trigger may target the controller, who loses 2 and gains 2")
    void etbCanTargetController() {
        castBat(player1.getId());
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("ETB trigger goes on the stack with the chosen target after the creature resolves")
    void etbTriggerCarriesTarget() {
        castBat(player2.getId());
        harness.passBothPriorities(); // resolve creature spell

        harness.assertOnBattlefield(player1, "Bloodhunter Bat");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
    }

    private void castBat(final java.util.UUID targetId) {
        harness.setHand(player1, List.of(new BloodhunterBat()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, targetId, null);
    }
}
