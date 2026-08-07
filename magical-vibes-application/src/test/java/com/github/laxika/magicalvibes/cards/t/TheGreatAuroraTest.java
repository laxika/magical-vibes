package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TheGreatAuroraTest extends BaseCardTest {

    private static List<Card> forests(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Forest());
        }
        return cards;
    }

    private void castAurora() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 9);
        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Each player shuffles their hand and owned permanents away and draws that many cards")
    void shufflesHandAndPermanentsAndDrawsThatMany() {
        harness.setHand(player1, List.of(new TheGreatAurora(), new Forest(), new Forest()));
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, forests(5));

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, forests(4));

        castAurora();

        // player1 shuffled two hand cards plus one permanent: library 5 + 3 = 8, draws 3.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();

        // player2 shuffled one hand card and owned nothing: library 4 + 1 = 5, draws 1.
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Each player may put any number of land cards from their new hand onto the battlefield")
    void eachPlayerMayPutLandsOntoBattlefield() {
        harness.setHand(player1, List.of(new TheGreatAurora(), new Forest()));
        harness.setLibrary(player1, forests(3));
        harness.setHand(player2, List.of(new Forest()));
        harness.setLibrary(player2, forests(3));

        castAurora();

        // Active player chooses first and puts their whole (all-land) hand onto the battlefield.
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutLandsFromHandChoice.class);
        harness.handleMultipleCardsChosen(player1, handCardIds(player1));

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        // Then the non-active player, who declines.
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutLandsFromHandChoice.class);
        harness.handleMultipleCardsChosen(player2, List.of());

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The Great Aurora exiles itself instead of going to the graveyard")
    void exilesItself() {
        harness.setHand(player1, List.of(new TheGreatAurora()));
        harness.setLibrary(player1, forests(3));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, forests(3));

        castAurora();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName).contains("The Great Aurora");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName).doesNotContain("The Great Aurora");
    }

    private List<UUID> handCardIds(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerHands.get(player.getId()).stream().map(Card::getId).toList();
    }
}
