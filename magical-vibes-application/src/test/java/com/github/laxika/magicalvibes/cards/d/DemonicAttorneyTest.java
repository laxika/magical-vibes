package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DemonicAttorney.class, GrizzlyBears.class, HillGiant.class})
class DemonicAttorneyTest extends BaseCardTest {

    @Test
    @DisplayName("Each player antes the top card of their library")
    void eachPlayerAntesTopCard() {
        Card player1Top = new GrizzlyBears();
        Card player1Remaining = new HillGiant();
        Card player2Top = new HillGiant();
        Card player2Remaining = new GrizzlyBears();
        harness.setLibrary(player1, List.of(player1Top, player1Remaining));
        harness.setLibrary(player2, List.of(player2Top, player2Remaining));
        harness.castFromHand(player1, new DemonicAttorney(), "{1}{B}{B}");
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId).containsExactly(player1Top.getId());
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId).containsExactly(player2Top.getId());
        assertThat(gd.antedCardIds)
                .containsExactlyInAnyOrder(player1Top.getId(), player2Top.getId());
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId).containsExactly(player1Remaining.getId());
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getId).containsExactly(player2Remaining.getId());
    }

    @Test
    @DisplayName("Does nothing for a player with an empty library")
    void emptyLibraryDoesNotAnte() {
        Card player1Top = new GrizzlyBears();
        harness.setLibrary(player1, List.of(player1Top));
        harness.setLibrary(player2, List.of());
        harness.castFromHand(player1, new DemonicAttorney(), "{1}{B}{B}");
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(player1Top);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An empty controller library does not stop another player from anting")
    void emptyControllerLibraryStillAntesOtherPlayersCard() {
        Card player2Top = new GrizzlyBears();
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(player2Top));
        harness.castFromHand(player1, new DemonicAttorney(), "{1}{B}{B}");
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId).containsExactly(player2Top.getId());
        assertThat(gd.antedCardIds).containsExactly(player2Top.getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }
}
