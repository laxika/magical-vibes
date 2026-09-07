package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.l.LeylineOfSanctity;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RazorPendulum.class)
class RazorPendulumTest extends BaseCardTest {

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }

    @Test
    @DisplayName("Deals 2 damage to the end-step player at 5 or less life")
    void damagesLowLifeEndStepPlayer() {
        harness.addToBattlefield(player1, new RazorPendulum());
        harness.setLife(player1, 5);
        harness.setLife(player2, 20);

        advanceToEndStep(player1);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does nothing when the end-step player has more than 5 life")
    void noDamageAboveThreshold() {
        harness.addToBattlefield(player1, new RazorPendulum());
        harness.setLife(player1, 6);

        advanceToEndStep(player1);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(6);
    }

    @Test
    @DisplayName("Hits the end-step player even when an opponent controls it")
    void hitsEndStepPlayerNotController() {
        harness.addToBattlefield(player2, new RazorPendulum());
        harness.setLife(player1, 4);
        harness.setLife(player2, 4);

        advanceToEndStep(player1);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(4);
    }

    @Test
    @DisplayName("Deals damage during the opponent's end step")
    void damagesOpponentEndStepPlayer() {
        harness.addToBattlefield(player1, new RazorPendulum());
        harness.setLife(player1, 20);
        harness.setLife(player2, 5);

        advanceToEndStep(player2);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(3);
    }

    @Test
    @DisplayName("Rechecks the life threshold when the ability resolves")
    void rechecksLifeThresholdAtResolution() {
        harness.addToBattlefield(player1, new RazorPendulum());
        harness.setLife(player1, 5);

        advanceToEndStep(player1);
        harness.setLife(player1, 6);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(6);
    }

    @Test
    @CardUsed(LeylineOfSanctity.class)
    @DisplayName("Deals damage even when the end-step player has hexproof")
    void nonTargetingAbilityIgnoresPlayerHexproof() {
        harness.addToBattlefield(player2, new RazorPendulum());
        harness.addToBattlefield(player1, new LeylineOfSanctity());
        harness.setLife(player1, 4);
        harness.setLife(player2, 20);

        advanceToEndStep(player1);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
