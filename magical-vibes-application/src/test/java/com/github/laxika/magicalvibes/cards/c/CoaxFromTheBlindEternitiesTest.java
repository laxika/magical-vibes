package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.ElderDeepFiend;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CoaxFromTheBlindEternitiesTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a chosen Eldrazi card from outside the game into hand")
    void putsChosenSideboardEldraziIntoHand() {
        Card eldrazi = new ElderDeepFiend();
        Card nonEldrazi = new GrizzlyBears();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(eldrazi, nonEldrazi)));
        castCoax();

        PendingInteraction.SearchOutsideGameOrExileCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchOutsideGameOrExileCardChoice.class);
        assertThat(choice.validCardIds()).contains(eldrazi.getId()).doesNotContain(nonEldrazi.getId());

        harness.handleMultipleCardsChosen(player1, List.of(eldrazi.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(eldrazi);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(nonEldrazi);
    }

    @Test
    @DisplayName("Returns a face-up Eldrazi card from exile to hand")
    void returnsFaceUpExiledEldraziToHand() {
        Card eldrazi = new ElderDeepFiend();
        gd.addToExile(player1.getId(), eldrazi);
        castCoax();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.SearchOutsideGameOrExileCardChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(eldrazi.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(eldrazi);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(eldrazi);
    }

    @Test
    @DisplayName("Ignores face-down and opponent-owned Eldrazi cards in exile")
    void ignoresIneligibleExiledEldraziCards() {
        Card faceDown = new ElderDeepFiend();
        Card opponentCard = new ElderDeepFiend();
        gd.addToExile(player1.getId(), faceDown, null, true);
        gd.addToExile(player2.getId(), opponentCard);
        castCoax();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(faceDown, opponentCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(faceDown);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(opponentCard);
    }

    @Test
    @DisplayName("May decline to take an eligible Eldrazi card")
    void mayDecline() {
        Card eldrazi = new ElderDeepFiend();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(eldrazi)));
        castCoax();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(eldrazi);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(eldrazi);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castCoax() {
        harness.setHand(player1, List.of(new CoaxFromTheBlindEternities()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
