package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Chronosavant.class)
@DisplayName("Chronosavant")
class ChronosavantTest extends BaseCardTest {

    @Test
    @DisplayName("Returns itself from the graveyard to the battlefield tapped")
    void returnsFromGraveyardTapped() {
        harness.setGraveyard(player1, List.of(new Chronosavant()));
        addActivationMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent chronosavant = findPermanent(player1, "Chronosavant");
        assertThat(chronosavant.isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Chronosavant");
    }

    @Test
    @DisplayName("Queues a skip of its controller's next turn")
    void queuesSkipOfNextTurn() {
        harness.setGraveyard(player1, List.of(new Chronosavant()));
        addActivationMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.skipNextTurnCount.getOrDefault(player1.getId(), 0)).isEqualTo(1);
        assertThat(gd.skipNextTurnCount.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Skips its controller's next turn")
    void skipsNextTurn() {
        harness.setGraveyard(player1, List.of(new Chronosavant()));
        addActivationMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        endTurn();
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());

        endTurn();
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.skipNextTurnCount.getOrDefault(player1.getId(), 0)).isEqualTo(0);
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void endTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
