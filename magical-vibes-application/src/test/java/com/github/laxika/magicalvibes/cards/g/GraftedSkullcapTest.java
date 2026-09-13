package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GraftedSkullcap.class, CoralMerfolk.class})
class GraftedSkullcapTest extends BaseCardTest {

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2; // avoid first-turn draw skip
        harness.forceStep(TurnStep.UPKEEP);
        harness.passUntil(activePlayer, TurnStep.DRAW);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }

    @Test
    @DisplayName("Draw step draws an additional card")
    void drawStepDrawsAdditionalCard() {
        harness.addToBattlefield(player1, new GraftedSkullcap());
        harness.setHand(player1, List.of());

        advanceToDraw(player1);
        harness.passBothPriorities(); // resolve draw trigger

        // Normal draw (1) + additional draw (1)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Extra card only on controller's draw step, not opponent's")
    void extraCardOnlyOnControllersDrawStep() {
        harness.addToBattlefield(player1, new GraftedSkullcap());
        harness.setHand(player2, List.of());

        advanceToDraw(player2);

        // Only the normal draw — no trigger on the opponent's draw step
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("End step discards the controller's entire hand")
    void endStepDiscardsHand() {
        harness.addToBattlefield(player1, new GraftedSkullcap());
        harness.setHand(player1, List.of(new CoralMerfolk(), new CoralMerfolk()));

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Coral Merfolk"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Discard only on controller's end step, not opponent's")
    void discardOnlyOnControllersEndStep() {
        harness.addToBattlefield(player1, new GraftedSkullcap());
        harness.setHand(player2, List.of(new CoralMerfolk()));

        advanceToEndStep(player2);

        // Opponent's hand is untouched by the controller's Skullcap
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Draw trigger resolves even if Grafted Skullcap leaves before resolution")
    void drawTriggerResolvesAfterSkullcapLeavesBattlefield() {
        var skullcap = harness.addToBattlefieldAndReturn(player1, new GraftedSkullcap());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new CoralMerfolk(), new CoralMerfolk()));

        advanceToDraw(player1);
        gd.playerBattlefields.get(player1.getId()).remove(skullcap);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("End-step trigger resolves even if Grafted Skullcap leaves before resolution")
    void endStepTriggerResolvesAfterSkullcapLeavesBattlefield() {
        var skullcap = harness.addToBattlefieldAndReturn(player1, new GraftedSkullcap());
        harness.setHand(player1, List.of(new CoralMerfolk(), new CoralMerfolk()));

        advanceToEndStep(player1);
        gd.playerBattlefields.get(player1.getId()).remove(skullcap);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Coral Merfolk"))
                .hasSize(2);
    }
}
