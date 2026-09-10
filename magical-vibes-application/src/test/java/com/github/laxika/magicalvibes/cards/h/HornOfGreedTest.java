package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.v.VolrathsStronghold;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HornOfGreed.class, VolrathsStronghold.class})
class HornOfGreedTest extends BaseCardTest {

    @Test
    @DisplayName("Controller draws a card when playing a land")
    void controllerDrawsWhenPlayingLand() {
        harness.addToBattlefield(player1, new HornOfGreed());
        harness.setHand(player1, List.of(new VolrathsStronghold()));
        harness.setLibrary(player1, List.of(new VolrathsStronghold()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The player who plays a land draws when an opponent controls Horn of Greed")
    void playerWhoPlaysLandDraws() {
        harness.addToBattlefield(player1, new HornOfGreed());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new VolrathsStronghold()));
        harness.setHand(player2, List.of(new VolrathsStronghold()));
        harness.setLibrary(player2, List.of(new VolrathsStronghold()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Each Horn of Greed triggers when its controller plays a land")
    void eachHornTriggersSeparately() {
        harness.addToBattlefield(player1, new HornOfGreed());
        harness.addToBattlefield(player1, new HornOfGreed());
        harness.setHand(player1, List.of(new VolrathsStronghold()));
        harness.setLibrary(player1, List.of(new VolrathsStronghold(), new VolrathsStronghold()));

        harness.playLand(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A land entering without being played does not trigger Horn of Greed")
    void landEnteringWithoutBeingPlayedDoesNotTrigger() {
        harness.addToBattlefield(player1, new HornOfGreed());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new VolrathsStronghold()));

        harness.enterBattlefieldAndReturn(player2, new VolrathsStronghold());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }
}
