package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.n.NessianCourser;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.PayManaOrLoseGameAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PactOfNegation.class, NessianCourser.class})
class PactOfNegationTest extends BaseCardTest {

    private void castPact() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        NessianCourser courser = new NessianCourser();
        harness.castFromHand(player1, courser, "{2}{G}");

        harness.setHand(player2, List.of(new PactOfNegation()));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, courser.getId());
        harness.passBothPriorities();
    }

    private void reachPactUpkeepPrompt() {
        advanceToUpkeep(player2);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Counters the target spell and schedules its upkeep payment")
    void countersAndSchedulesPayment() {
        castPact();

        harness.assertNotOnBattlefield(player1, "Nessian Courser");
        harness.assertInGraveyard(player1, "Nessian Courser");

        List<PayManaOrLoseGameAtNextUpkeep> scheduled = gd.getDelayedActions(PayManaOrLoseGameAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().playerId()).isEqualTo(player2.getId());
        assertThat(scheduled.getFirst().manaCost()).isEqualTo("{3}{U}{U}");
    }

    @Test
    @DisplayName("Waits for the Pact controller's next upkeep")
    void waitsForControllerNextUpkeep() {
        castPact();
        advanceToUpkeep(player1);

        assertThat(gd.currentStep).isEqualTo(TurnStep.UPKEEP);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getDelayedActions(PayManaOrLoseGameAtNextUpkeep.class)).hasSize(1);
    }

    @Test
    @DisplayName("Paying {3}{U}{U} at the next upkeep avoids losing the game")
    void payingAvoidsLoss() {
        castPact();
        reachPactUpkeepPrompt();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.getDelayedActions(PayManaOrLoseGameAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("Declining the next-upkeep payment loses the game")
    void decliningCausesLoss() {
        castPact();
        reachPactUpkeepPrompt();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }
}
