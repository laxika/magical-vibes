package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PraetorsCounsel.class, GrizzlyBears.class, Forest.class, Mountain.class})
class PraetorsCounselTest extends BaseCardTest {

    @Test
    @DisplayName("Returns all cards from graveyard to hand")
    void returnsAllCardsFromGraveyardToHand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Put some cards in graveyard
        Card bears = new GrizzlyBears();
        Card mountain = new Mountain();
        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(bears, mountain, forest));

        harness.castFromHand(player1, new PraetorsCounsel(), "{5}{G}{G}{G}");
        harness.passBothPriorities();

        // All 3 graveyard cards should now be in hand
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        List<Card> hand = gd.playerHands.get(player1.getId());
        assertThat(hand).contains(bears, mountain, forest);
    }

    @Test
    @DisplayName("Works with empty graveyard")
    void worksWithEmptyGraveyard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new PraetorsCounsel(), "{5}{G}{G}{G}");
        harness.passBothPriorities();

        // No errors, graveyard still empty
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Praetor's Counsel is exiled after resolution, not put in graveyard")
    void exiledAfterResolution() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        PraetorsCounsel counsel = new PraetorsCounsel();
        harness.castFromHand(player1, counsel, "{5}{G}{G}{G}");
        harness.passBothPriorities();

        // Should be in exile, not graveyard
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(counsel);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(counsel);
    }

    @Test
    @DisplayName("Grants no maximum hand size for the rest of the game")
    void grantsNoMaxHandSize() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new PraetorsCounsel(), "{5}{G}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.playersWithNoMaximumHandSize).contains(player1.getId());
    }

    @Test
    @DisplayName("Player does not have to discard during cleanup after Praetor's Counsel")
    void noDiscardDuringCleanup() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Put many cards in graveyard so hand exceeds 7 after resolution
        for (int i = 0; i < 10; i++) {
            gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());
        }

        harness.castFromHand(player1, new PraetorsCounsel(), "{5}{G}{G}{G}");
        harness.passBothPriorities();

        // Hand should have 10 cards (all returned from graveyard)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(10);

        // Advance to cleanup; no discard should be required
        harness.forceStep(TurnStep.END_STEP);
        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
    }

    @Test
    @DisplayName("Opponent is not affected by Praetor's Counsel hand size grant")
    void opponentNotAffected() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new PraetorsCounsel(), "{5}{G}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.playersWithNoMaximumHandSize).doesNotContain(player2.getId());

        // Verify opponent still has to discard with 9 cards
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new Forest(), new Forest(), new Forest(),
                new Mountain(), new Mountain(), new Mountain()
        )));

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Does not return cards from opponent's graveyard")
    void doesNotReturnOpponentGraveyard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card opponentCard = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(opponentCard));

        harness.castFromHand(player1, new PraetorsCounsel(), "{5}{G}{G}{G}");
        harness.passBothPriorities();

        // Opponent's graveyard should be untouched
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentCard);
    }

    @Test
    @DisplayName("Returns another Praetor's Counsel without returning the resolving spell")
    void returnsAnotherCounselFromGraveyard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        PraetorsCounsel buriedCounsel = new PraetorsCounsel();
        PraetorsCounsel resolvingCounsel = new PraetorsCounsel();
        harness.setGraveyard(player1, List.of(buriedCounsel));

        harness.castFromHand(player1, resolvingCounsel, "{5}{G}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(buriedCounsel);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(resolvingCounsel);
    }

    @Test
    @DisplayName("An empty graveyard still grants no maximum hand size on later turns")
    void emptyGraveyardGrantPersistsAcrossTurns() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of());
        harness.setHand(player2, List.of());

        harness.castFromHand(player1, new PraetorsCounsel(), "{5}{G}{G}{G}");
        harness.passBothPriorities();

        List<Card> oversizedHand = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            oversizedHand.add(new Forest());
        }
        harness.setHand(player1, oversizedHand);
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        int handSizeBeforeCleanup = gd.playerHands.get(player1.getId()).size();
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBeforeCleanup);
        assertThat(gd.playerHands.get(player1.getId())).containsAll(oversizedHand);
    }
}
