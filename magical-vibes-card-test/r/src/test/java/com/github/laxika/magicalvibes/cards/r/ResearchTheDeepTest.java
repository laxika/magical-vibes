package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.testutil.FakeConnection;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.cards.m.MothdustChangeling;
import com.github.laxika.magicalvibes.cards.m.Mutavault;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ResearchTheDeep.class, MothdustChangeling.class, Mutavault.class})
class ResearchTheDeepTest extends BaseCardTest {

    private void castResearch() {
        harness.castFromHand(player1, new ResearchTheDeep(), "{1}{U}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Draws the top card before a tied clash and puts the spell in the graveyard")
    void drawsACard() {
        // Draw happens first (Mutavault), then the clash reveals the next card (Mutavault, MV 0)
        // against the opponent's Mutavault (MV 0), so no one wins.
        harness.setLibrary(player1, List.of(new Mutavault(), new Mutavault()));
        harness.setLibrary(player2, List.of(new Mutavault()));

        castResearch();
        keepClashCardsOnTop();

        harness.assertInHand(player1, "Mutavault");
        harness.assertInGraveyard(player1, "Research the Deep");
    }

    @Test
    @DisplayName("Winning the clash returns Research the Deep to its owner's hand")
    void wonClashReturnsSpellToHand() {
        // Draw Mutavault, then clash with Mothdust Changeling (MV 1) vs opponent's Mutavault
        // (MV 0), so player1 wins.
        harness.setLibrary(player1, List.of(new Mutavault(), new MothdustChangeling()));
        harness.setLibrary(player2, List.of(new Mutavault()));

        castResearch();
        keepClashCardsOnTop();

        harness.assertInHand(player1, "Research the Deep");
        harness.assertNotInGraveyard(player1, "Research the Deep");
    }

    @Test
    @DisplayName("Losing the clash sends Research the Deep to the graveyard")
    void lostClashSendsSpellToGraveyard() {
        // Draw Mutavault, then clash with Mutavault (MV 0) vs opponent's Mothdust Changeling
        // (MV 1), so player1 loses.
        harness.setLibrary(player1, List.of(new Mutavault(), new Mutavault()));
        harness.setLibrary(player2, List.of(new MothdustChangeling()));

        castResearch();
        keepClashCardsOnTop();

        harness.assertInGraveyard(player1, "Research the Deep");
        harness.assertNotInHand(player1, "Research the Deep");
    }

    @Test
    @DisplayName("Offers a choice of putting the revealed cards on top or bottom")
    void offersLibraryPlacementChoicesAfterClash() {
        harness.setLibrary(player1, List.of(new Mutavault(), new MothdustChangeling()));
        harness.setLibrary(player2, List.of(new Mutavault()));

        castResearch();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(((PendingInteraction.Scry) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player1.getId());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(((PendingInteraction.Scry) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void keepClashCardsOnTop() {
        while (gd.interaction.activeInteraction() instanceof PendingInteraction.Scry scry) {
            var player = scry.playerId().equals(player1.getId()) ? player1 : player2;
            gs.handleInteractionAnswer(gd, player, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        }
    }

    @Test
    @DisplayName("Bottoming both revealed cards does not change the clash winner")
    void bottomingCardsPreservesWin() {
        var revealed = new MothdustChangeling();
        var opponentRevealed = new Mutavault();
        harness.setLibrary(player1, List.of(new Mutavault(), revealed, new Mutavault()));
        harness.setLibrary(player2, List.of(opponentRevealed, new MothdustChangeling()));

        castResearch();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId()).getLast()).isSameAs(revealed);
        assertThat(gd.playerDecks.get(player2.getId()).getLast()).isSameAs(opponentRevealed);
        harness.assertInHand(player1, "Research the Deep");
        harness.assertNotInGraveyard(player1, "Research the Deep");
    }

    @Test
    @DisplayName("Drawing the last library card leaves no revealed card and cannot win the clash")
    void emptyLibraryAfterDrawCannotWin() {
        harness.setLibrary(player1, List.of(new Mutavault()));
        harness.setLibrary(player2, List.of(new Mutavault()));

        castResearch();
        keepClashCardsOnTop();

        harness.assertInHand(player1, "Mutavault");
        harness.assertInGraveyard(player1, "Research the Deep");
        harness.assertNotInHand(player1, "Research the Deep");
    }

    @Test
    @DisplayName("In a three-player game the controller chooses the opponent to clash with")
    void controllerChoosesTheOpponentToClashWith() {
        Player player3 = addThirdPlayer();
        // Draw Mutavault, then clash with Mothdust Changeling (MV 1): a tie against the second player's
        // Mothdust Changeling, a win against the third player's Mutavault (MV 0).
        harness.setLibrary(player1, List.of(new Mutavault(), new MothdustChangeling()));
        harness.setLibrary(player2, List.of(new MothdustChangeling()));
        harness.setLibrary(player3, List.of(new Mutavault()));

        castResearch();

        PendingInteraction.PermanentChoice opponentChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(opponentChoice).isNotNull();
        assertThat(opponentChoice.validIds()).containsExactlyInAnyOrder(player2.getId(), player3.getId());
        harness.handlePermanentChosen(player1, player3.getId());
        while (gd.interaction.activeInteraction() instanceof PendingInteraction.Scry scry) {
            var player = scry.playerId().equals(player1.getId()) ? player1
                    : scry.playerId().equals(player2.getId()) ? player2 : player3;
            gs.handleInteractionAnswer(gd, player, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        }

        harness.assertInHand(player1, "Research the Deep");
    }

    private Player addThirdPlayer() {
        UUID thirdPlayerId = UUID.randomUUID();
        Player player3 = new Player(thirdPlayerId, "Charlie");
        gd.playerIds.add(thirdPlayerId);
        gd.orderedPlayerIds.add(thirdPlayerId);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(thirdPlayerId, "Charlie");
        gd.playerDecks.put(thirdPlayerId, new ArrayList<>());
        gd.playerHands.put(thirdPlayerId, new ArrayList<>());
        gd.playerBattlefields.put(thirdPlayerId, new ArrayList<>());
        gd.playerGraveyards.put(thirdPlayerId, new ArrayList<>());
        gd.playerCommandZones.put(thirdPlayerId, new ArrayList<>());
        gd.playerManaPools.put(thirdPlayerId, new ManaPool());
        gd.playerLifeTotals.put(thirdPlayerId, 20);
        harness.getSessionManager().registerPlayer(
                new FakeConnection("conn-3"), thirdPlayerId, "Charlie");
        return player3;
    }

}
