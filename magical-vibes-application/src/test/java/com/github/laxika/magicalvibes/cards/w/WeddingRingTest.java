package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.Revitalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WeddingRing.class, Revitalize.class})
class WeddingRingTest extends BaseCardTest {

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void castWeddingRingPair() {
        harness.setHand(player1, List.of(new WeddingRing()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Casting Wedding Ring gives the targeted opponent a token copy")
    void givesTargetedOpponentTokenCopy() {
        castWeddingRingPair();

        assertThat(findPermanents(player1, "Wedding Ring")).hasSize(1);
        List<Permanent> opponentRings = findPermanents(player2, "Wedding Ring");
        assertThat(opponentRings).hasSize(1);
        assertThat(opponentRings.getFirst().getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Wedding Ring's copy ability draws for its controller when an opponent draws during their turn")
    void opponentDrawsForControllerDuringTheirTurn() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        castWeddingRingPair();

        int controllerHandBefore = gd.playerHands.get(player1.getId()).size();

        advanceToDraw(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(controllerHandBefore + 1);
    }

    @Test
    @DisplayName("Wedding Ring's copy ability gains life when an opponent gains life during their turn")
    void opponentGainsLifeForControllerDuringTheirTurn() {
        harness.addToBattlefield(player1, new WeddingRing());
        harness.addToBattlefield(player2, new WeddingRing());
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new Revitalize()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
    }
}
