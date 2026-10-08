package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.m.MoggSentry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UrzasGuilt.class, MoggSentry.class})
class UrzasGuiltTest extends BaseCardTest {

    @Test
    @DisplayName("Each player draws two, discards three, and loses 4 life")
    void eachPlayerDrawsDiscardsAndLosesLife() {
        harness.setHand(player1, List.of(
                new UrzasGuilt(), new MoggSentry(), new MoggSentry(), new MoggSentry(), new MoggSentry()));
        harness.setHand(player2, List.of(
                new MoggSentry(), new MoggSentry(), new MoggSentry(), new MoggSentry()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(6);

        for (int i = 0; i < 6; i++) {
            harness.handleCardChosen(i < 3 ? player1 : player2, 0);
        }

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Urza's Guilt");
    }

    @Test
    @DisplayName("A player with fewer than three cards discards their whole hand and still loses 4 life")
    void playerWithFewerThanThreeCardsDiscardsWholeHand() {
        harness.setHand(player1, List.of(
                new UrzasGuilt(), new MoggSentry(), new MoggSentry(), new MoggSentry(), new MoggSentry()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new MoggSentry(), new MoggSentry()));
        harness.setLibrary(player2, List.of(new MoggSentry(), new MoggSentry()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);

        for (int i = 0; i < 3; i++) {
            harness.handleCardChosen(player1, 0);
        }
        for (int i = 0; i < 2; i++) {
            harness.handleCardChosen(player2, 0);
        }

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Discard choices stay in hand until all players have chosen, then happen simultaneously")
    void discardsWaitForAllPlayersToChoose() {
        harness.setHand(player1, List.of(new UrzasGuilt(), new MoggSentry(), new MoggSentry()));
        harness.setHand(player2, List.of(new MoggSentry(), new MoggSentry()));
        harness.setLibrary(player1, List.of(new MoggSentry(), new MoggSentry()));
        harness.setLibrary(player2, List.of(new MoggSentry(), new MoggSentry()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        int graveyardSizeBeforeChoices = gd.playerGraveyards.get(player1.getId()).size();
        for (int i = 0; i < 3; i++) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardSizeBeforeChoices);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        for (int i = 0; i < 3; i++) {
            harness.handleCardChosen(player2, 0);
        }

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        harness.assertLife(player1, 16);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Both players with short hands discard the cards they just drew and lose life")
    void bothPlayersDiscardOnlyCardsJustDrawn() {
        harness.setHand(player1, List.of(new UrzasGuilt()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new MoggSentry(), new MoggSentry()));
        harness.setLibrary(player2, List.of(new MoggSentry(), new MoggSentry()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        for (int i = 0; i < 2; i++) {
            harness.handleCardChosen(player1, 0);
        }
        for (int i = 0; i < 2; i++) {
            harness.handleCardChosen(player2, 0);
        }

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        harness.assertLife(player1, 16);
        harness.assertLife(player2, 16);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
