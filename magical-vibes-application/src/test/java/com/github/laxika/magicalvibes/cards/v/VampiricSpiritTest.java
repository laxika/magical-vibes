package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(VampiricSpirit.class)
class VampiricSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the creature spell puts the ETB trigger on the stack")
    void resolvingPutsEtbOnStack() {
        castVampiricSpirit();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("ETB trigger makes the controller lose 4 life")
    void etbMakesControllerLose4Life() {
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        castVampiricSpirit();
        resolveAllTriggers();

        harness.assertLife(player1, lifeBefore - 4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opponent's life is unaffected by the ETB trigger")
    void opponentLifeUnaffected() {
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        castVampiricSpirit();
        resolveAllTriggers();

        harness.assertLife(player2, opponentLifeBefore);
    }

    @Test
    @DisplayName("The creature's controller loses 4 life when an opponent casts it")
    void opponentControllerLosesLife() {
        int playerLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        castVampiricSpirit(player2);
        resolveAllTriggers();

        harness.assertLife(player1, playerLifeBefore);
        harness.assertLife(player2, opponentLifeBefore - 4);
    }

    @Test
    @DisplayName("Life loss waits until the ETB trigger resolves")
    void lifeLossWaitsForTriggerResolution() {
        harness.setLife(player1, 20);

        castVampiricSpirit();
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);

        resolveAllTriggers();

        harness.assertLife(player1, 16);
    }

    @Test
    @DisplayName("Entering without being cast still causes life loss for each Spirit")
    void eachNoncastEntryCausesLifeLoss() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.enterBattlefieldAndReturn(player1, new VampiricSpirit());
        resolveAllTriggers();
        harness.assertLife(player1, 16);

        harness.enterBattlefieldAndReturn(player1, new VampiricSpirit());
        resolveAllTriggers();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Life loss is mandatory even when the controller has less than 4 life")
    void lifeLossCanBeLethal() {
        harness.setLife(player1, 3);
        harness.setLife(player2, 20);

        castVampiricSpirit();
        harness.passBothPriorities();
        harness.assertLife(player1, 3);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);

        harness.passBothPriorities();

        harness.assertLife(player1, -1);
        harness.assertLife(player2, 20);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    private void castVampiricSpirit() {
        castVampiricSpirit(player1);
    }

    private void castVampiricSpirit(Player player) {
        harness.forceActivePlayer(player);
        harness.castFromHand(player, new VampiricSpirit(), "{2}{B}{B}");
    }
}
