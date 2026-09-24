package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WeddingRing.class})
class WeddingRingTestMarRegression extends BaseCardTest {

    @Test
    @DisplayName("A cast Wedding Ring gives the targeted opponent a token copy")
    void castCreatesTokenCopyForTargetOpponent() {
        harness.setHand(player1, java.util.List.of(new WeddingRing()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Wedding Ring")).isEqualTo(1);
        assertThat(findPermanents(player2, "Wedding Ring")).singleElement()
                .extracting(permanent -> permanent.getCard().isToken())
                .isEqualTo(true);
    }

    @Test
    @DisplayName("An opponent with Wedding Ring draws a card for the controller during their turn")
    void opponentDrawsDuringTheirTurn() {
        harness.addToBattlefield(player1, new WeddingRing());
        harness.addToBattlefield(player2, new WeddingRing());
        int controllerHandBefore = gd.playerHands.get(player1.getId()).size();

        advanceToDraw(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(controllerHandBefore + 1);
    }

    @Test
    @DisplayName("An opponent with Wedding Ring makes the controller gain that much life during their turn")
    void opponentGainsLifeDuringTheirTurn() {
        harness.addToBattlefield(player1, new WeddingRing());
        harness.addToBattlefield(player2, new WeddingRing());
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 3));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("The opponent life-gain trigger does not fire outside that opponent's turn")
    void opponentLifeGainOutsideTheirTurnDoesNotTrigger() {
        harness.addToBattlefield(player1, new WeddingRing());
        harness.addToBattlefield(player2, new WeddingRing());
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 3));

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
