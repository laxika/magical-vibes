package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FoulImp.class})
class FoulImpTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the creature spell puts the ETB trigger on the stack")
    void resolvingPutsEtbOnStack() {
        castFoulImp();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("ETB trigger makes the controller lose 2 life")
    void etbMakesControllerLose2Life() {
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        castFoulImp();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering the battlefield without being cast still causes the life loss")
    void enteringWithoutBeingCastMakesControllerLose2Life() {
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.enterBattlefieldAndReturn(player1, new FoulImp());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Opponent's life is unaffected by the ETB trigger")
    void opponentLifeUnaffected() {
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        castFoulImp();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    private void castFoulImp() {
        harness.castFromHand(player1, new FoulImp(), "{B}{B}");
    }

    @Test
    @DisplayName("Life loss waits until the entry trigger resolves")
    void lifeLossWaitsForTriggerResolution() {
        harness.setLife(player1, 20);

        castFoulImp();
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Entering under the opponent's control makes that player lose life")
    void opponentControlledEntryMakesOpponentLoseLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.enterBattlefieldAndReturn(player2, new FoulImp());
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Each entering copy causes its own life loss")
    void multipleEntriesEachCauseLifeLoss() {
        harness.setLife(player1, 20);

        harness.enterBattlefieldAndReturn(player1, new FoulImp());
        resolveAllTriggers();
        harness.assertLife(player1, 18);

        harness.enterBattlefieldAndReturn(player1, new FoulImp());
        resolveAllTriggers();

        harness.assertLife(player1, 16);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }
}
