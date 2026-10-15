package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.Colossapede;
import com.github.laxika.magicalvibes.cards.d.DuneBeetle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MiasmicMummy.class, DuneBeetle.class, Colossapede.class})
class MiasmicMummyTest extends BaseCardTest {

    // "When this creature enters, each player discards a card."

    private void castMiasmicMummy() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
    }

    @Test
    @DisplayName("Players choose in APNAP order, then discard simultaneously")
    void eachPlayerDiscards() {
        harness.setHand(player1, List.of(new MiasmicMummy(), new DuneBeetle()));
        harness.setHand(player2, List.of(new Colossapede()));

        castMiasmicMummy();
        resolveAllTriggers();

        // The active player chooses first, without revealing the card to the opponent.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);

        // No discard occurs until both players have chosen.
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Dune Beetle");
        harness.assertInGraveyard(player2, "Colossapede");
    }

    @Test
    @DisplayName("A player with an empty hand discards nothing")
    void emptyHandDiscardsNothing() {
        harness.setHand(player1, List.of(new MiasmicMummy()));
        harness.setHand(player2, List.of(new Colossapede()));

        castMiasmicMummy();
        resolveAllTriggers();

        // Player1's hand is empty after casting, so only player2 is prompted.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Colossapede");
    }

    @Test
    @DisplayName("The trigger completes without a choice when both hands are empty")
    void bothHandsEmpty() {
        harness.setHand(player2, List.of());
        harness.castFromHand(player1, new MiasmicMummy(), "{1}{B}");

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Miasmic Mummy");
    }

    @Test
    @DisplayName("Each player chooses exactly one card from a larger hand")
    void eachPlayerChoosesOneCard() {
        DuneBeetle keptByController = new DuneBeetle();
        Colossapede discardedByController = new Colossapede();
        Colossapede keptByOpponent = new Colossapede();
        DuneBeetle discardedByOpponent = new DuneBeetle();
        harness.setHand(player1, List.of(new MiasmicMummy(), keptByController, discardedByController));
        harness.setHand(player2, List.of(keptByOpponent, discardedByOpponent));

        castMiasmicMummy();
        resolveAllTriggers();
        harness.handleCardChosen(player1, 1);
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keptByController);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(keptByOpponent);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discardedByController);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discardedByOpponent);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
