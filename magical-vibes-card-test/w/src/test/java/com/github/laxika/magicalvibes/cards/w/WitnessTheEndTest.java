package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WitnessTheEnd.class, GrizzlyBears.class, Forest.class})
class WitnessTheEndTest extends BaseCardTest {

    @Test
    @DisplayName("Target opponent exiles two cards and loses 2 life")
    void exilesTwoCardsAndCausesLifeLoss() {
        GrizzlyBears firstCard = new GrizzlyBears();
        Forest secondCard = new Forest();
        GrizzlyBears remainingCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new WitnessTheEnd()));
        harness.setHand(player2, List.of(firstCard, secondCard, remainingCard));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExileFromHandChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactly(firstCard, secondCard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(remainingCard);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Exiles all cards from a shorter hand and still causes life loss")
    void exilesAllCardsFromShorterHand() {
        Forest onlyCard = new Forest();
        harness.setHand(player1, List.of(new WitnessTheEnd()));
        harness.setHand(player2, List.of(onlyCard));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handleCardChosen(player2, 0);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(onlyCard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Cannot target yourself or a creature")
    void onlyTargetsOpponents() {
        harness.setHand(player1, List.of(new WitnessTheEnd()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);

        var creature = addCreatureReady(player2, new GrizzlyBears());
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent with an empty hand still loses 2 life")
    void emptyHandStillLosesLife() {
        harness.setHand(player1, List.of(new WitnessTheEnd()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Witness the End");
    }

    @Test
    @DisplayName("The opponent chooses which cards to exile before losing life")
    void opponentChoosesCardsBeforeLifeLoss() {
        WitnessTheEnd keptCard = new WitnessTheEnd();
        WitnessTheEnd firstExiledCard = new WitnessTheEnd();
        WitnessTheEnd secondExiledCard = new WitnessTheEnd();
        harness.setHand(player1, List.of(new WitnessTheEnd()));
        harness.setHand(player2, List.of(keptCard, firstExiledCard, secondExiledCard));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.handleCardChosen(player2, 1);
        harness.assertLife(player2, 20);
        harness.handleCardChosen(player2, 1);

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactly(firstExiledCard, secondExiledCard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(keptCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Witness the End");
    }
}
