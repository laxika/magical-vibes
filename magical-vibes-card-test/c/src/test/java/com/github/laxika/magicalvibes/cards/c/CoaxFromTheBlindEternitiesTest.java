package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.ArcaneAdaptation;
import com.github.laxika.magicalvibes.cards.e.ElderDeepFiend;
import com.github.laxika.magicalvibes.cards.l.LaboratoryBrute;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CoaxFromTheBlindEternities.class, ElderDeepFiend.class, ArcaneAdaptation.class, LaboratoryBrute.class})
class CoaxFromTheBlindEternitiesTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a chosen Eldrazi card from outside the game into hand")
    void putsChosenSideboardEldraziIntoHand() {
        Card eldrazi = new ElderDeepFiend();
        Card nonEldrazi = new CoaxFromTheBlindEternities();
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

    @Test
    @DisplayName("Chooses only one card when both outside the game and exile have eligible cards")
    void choosesOneCardAcrossBothZones() {
        Card sideboardCard = new ElderDeepFiend();
        Card exiledCard = new ElderDeepFiend();
        Card nonEldrazi = new CoaxFromTheBlindEternities();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(sideboardCard)));
        gd.addToExile(player1.getId(), exiledCard);
        gd.addToExile(player1.getId(), nonEldrazi);
        castCoax();

        PendingInteraction.SearchOutsideGameOrExileCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchOutsideGameOrExileCardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(sideboardCard.getId(), exiledCard.getId());
        harness.handleMultipleCardsChosen(player1, List.of(exiledCard.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(exiledCard);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(sideboardCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(nonEldrazi);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("May leave an eligible face-up Eldrazi in exile")
    void mayDeclineExiledCard() {
        Card eldrazi = new ElderDeepFiend();
        gd.addToExile(player1.getId(), eldrazi);
        castCoax();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(eldrazi);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Arcane Adaptation cannot make a sideboard creature an eligible Eldrazi")
    void subtypeGrantDoesNotAffectOutsideGameCards() {
        harness.addToBattlefieldAndReturn(player1, new ArcaneAdaptation()).setChosenSubtype(CardSubtype.ELDRAZI);
        Card nonEldrazi = new LaboratoryBrute();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(nonEldrazi)));
        castCoax();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(nonEldrazi);
    }

    @Test
    @DisplayName("Arcane Adaptation makes a face-up exiled creature an eligible Eldrazi")
    void subtypeGrantAffectsFaceUpExiledCards() {
        harness.addToBattlefieldAndReturn(player1, new ArcaneAdaptation()).setChosenSubtype(CardSubtype.ELDRAZI);
        Card creature = new LaboratoryBrute();
        gd.addToExile(player1.getId(), creature);
        castCoax();

        PendingInteraction.SearchOutsideGameOrExileCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchOutsideGameOrExileCardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castCoax() {
        harness.castFromHand(player1, new CoaxFromTheBlindEternities(), "{2}{U}");
        harness.passBothPriorities();
    }
}
