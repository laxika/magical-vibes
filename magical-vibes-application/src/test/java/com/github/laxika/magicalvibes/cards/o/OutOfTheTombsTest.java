package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OutOfTheTombs.class, GrizzlyBears.class, Island.class})
class OutOfTheTombsTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep adds two eon counters, then mills equal to the new total")
    void upkeepAddsCountersAndMills() {
        Permanent tombs = harness.addToBattlefieldAndReturn(player1, new OutOfTheTombs());
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));

        triggerUpkeep(player1);

        assertThat(tombs.getCounterCount(CounterType.EON)).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Island");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("An empty-library draw returns a creature from the graveyard to the battlefield")
    void emptyLibraryDrawReturnsCreature() {
        harness.addToBattlefield(player1, new OutOfTheTombs());
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(new Island(), new GrizzlyBears()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 1);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Island");
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("An empty-library draw loses when the graveyard has no creature card")
    void emptyLibraryDrawLosesWithoutCreature() {
        harness.addToBattlefield(player1, new OutOfTheTombs());
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(new Island()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Draws normally while the library has cards")
    void drawsNormallyWithCardsInLibrary() {
        harness.addToBattlefield(player1, new OutOfTheTombs());
        harness.setLibrary(player1, List.of(new Island()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        harness.assertInHand(player1, "Island");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    private void triggerUpkeep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
