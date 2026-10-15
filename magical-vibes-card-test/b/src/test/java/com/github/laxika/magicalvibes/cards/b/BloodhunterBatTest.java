package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodhunterBat.class, Murder.class})
class BloodhunterBatTest extends BaseCardTest {

    @Test
    @DisplayName("ETB trigger drains 2 life from the target opponent and gains 2 life")
    void etbDrainsTargetOpponent() {
        castBat(player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB trigger may target the controller, who loses 2 and gains 2")
    void etbCanTargetController() {
        castBat(player1.getId());
        resolveAllTriggers();

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

    @Test
    @DisplayName("The trigger gains life for its controller when the opponent casts the Bat")
    void opponentControlledBatGainsLifeForOpponent() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new BloodhunterBat()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.castCreature(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(22);
        harness.assertOnBattlefield(player2, "Bloodhunter Bat");
    }

    @Test
    @DisplayName("The ETB trigger resolves even after the Bat is destroyed")
    void triggerResolvesAfterBatIsDestroyed() {
        castBat(player2.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Bloodhunter Bat"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bloodhunter Bat");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);

        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Targeting yourself at 1 life does not cause a loss before the life gain")
    void selfTargetAtOneLifeSurvivesResolution() {
        harness.setLife(player1, 1);
        castBat(player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        assertThat(gd.stack).isEmpty();
    }

    private void castBat(final java.util.UUID targetId) {
        harness.setHand(player1, List.of(new BloodhunterBat()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0, targetId);
    }
}
