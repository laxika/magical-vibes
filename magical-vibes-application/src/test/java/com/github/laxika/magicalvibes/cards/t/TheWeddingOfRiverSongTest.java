package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheWeddingOfRiverSong.class, GrizzlyBears.class, Forest.class})
class TheWeddingOfRiverSongTest extends BaseCardTest {

    @Test
    void drawsAndLetsBothPlayersExileNonlandCardsWithManaValueTimeCounters() {
        TheWeddingOfRiverSong wedding = new TheWeddingOfRiverSong();
        GrizzlyBears controllerCard = new GrizzlyBears();
        Forest controllerLand = new Forest();
        GrizzlyBears opponentCard = new GrizzlyBears();
        Forest draw1 = new Forest();
        Forest draw2 = new Forest();
        Forest opponentDraw1 = new Forest();
        Forest opponentDraw2 = new Forest();

        harness.setHand(player1, List.of(wedding, controllerCard, controllerLand));
        harness.setHand(player2, List.of(opponentCard));
        harness.setLibrary(player1, List.of(draw1, draw2));
        harness.setLibrary(player2, List.of(opponentDraw1, opponentDraw2));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(controllerCard, controllerLand, draw1, draw2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.ExileNonlandCardFromHandWithTimeCountersChoice controllerChoice =
                gd.interaction.activeInteraction(PendingInteraction.ExileNonlandCardFromHandWithTimeCountersChoice.class);
        assertThat(controllerChoice).isNotNull();
        assertThat(controllerChoice.validIndices()).containsExactly(0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.playerHands.get(player2.getId()))
                .containsExactly(opponentCard, opponentDraw1, opponentDraw2);
        harness.handleMayAbilityChosen(player2, true);
        PendingInteraction.ExileNonlandCardFromHandWithTimeCountersChoice opponentChoice =
                gd.interaction.activeInteraction(PendingInteraction.ExileNonlandCardFromHandWithTimeCountersChoice.class);
        assertThat(opponentChoice).isNotNull();
        assertThat(opponentChoice.playerId()).isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(controllerCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(opponentCard);
        assertThat(gd.exiledCardTimeCounters)
                .containsEntry(controllerCard.getId(), 2)
                .containsEntry(opponentCard.getId(), 2);
        harness.handleListChoice(player1, "ADD");
        assertThat(gd.exiledCardTimeCounters)
                .containsEntry(controllerCard.getId(), 3)
                .containsEntry(opponentCard.getId(), 2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void bothPlayersDrawEvenWhenBothDeclineExile() {
        TheWeddingOfRiverSong controllerDraw = new TheWeddingOfRiverSong();
        TheWeddingOfRiverSong opponentDraw = new TheWeddingOfRiverSong();
        Forest controllerLand = new Forest();
        Forest opponentLand = new Forest();
        harness.setHand(player1, List.of(new TheWeddingOfRiverSong()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(controllerDraw, controllerLand));
        harness.setLibrary(player2, List.of(opponentDraw, opponentLand));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(controllerDraw, controllerLand);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentDraw, opponentLand);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @ParameterizedTest
    @CsvSource({"ADD, 4", "REMOVE, 2", "SKIP, 3"})
    void timeTravelsTheCardExiledDuringResolution(String action, int remainingCounters) {
        TheWeddingOfRiverSong exiledCard = new TheWeddingOfRiverSong();
        harness.setHand(player1, List.of(new TheWeddingOfRiverSong()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(exiledCard, new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player2, false);
        harness.handleListChoice(player1, action);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(exiledCard);
        assertThat(gd.exiledCardTimeCounters).containsEntry(exiledCard.getId(), remainingCounters);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
