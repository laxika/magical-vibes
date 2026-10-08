package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.Revitalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WeddingRing.class, Revitalize.class, GrizzlyBears.class})
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

        harness.castAndResolveInstant(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
    }

    @Test
    void tokenCopyDrawsForOpponentOnOriginalControllersTurn() {
        harness.setLibrary(player1, List.of(new WeddingRing()));
        harness.setLibrary(player2, List.of(new WeddingRing()));
        castWeddingRingPair();
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        advanceToDraw(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentDrawingOutsideTheirTurnDoesNotTrigger() {
        harness.addToBattlefield(player1, new WeddingRing());
        harness.addToBattlefield(player2, new WeddingRing());
        harness.setLibrary(player2, List.of(new WeddingRing()));
        harness.setHand(player2, List.of(new Revitalize()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.getLife(player1.getId());

        harness.castAndResolveInstant(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tokenCopyGainsLifeForOpponentOnOriginalControllersTurn() {
        castWeddingRingPair();
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 5));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(15);
        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copyTriggerStillResolvesAfterOriginalLeavesBattlefield() {
        harness.setHand(player1, List.of(new WeddingRing()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0, player2.getId());
        harness.passBothPriorities();
        Permanent original = findPermanents(player1, "Wedding Ring").getFirst();
        assertThat(gd.stack).hasSize(1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, original));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Wedding Ring")).isEmpty();
        assertThat(findPermanents(player2, "Wedding Ring")).singleElement()
                .satisfies(copy -> assertThat(copy.getCard().isToken()).isTrue());
    }
}
