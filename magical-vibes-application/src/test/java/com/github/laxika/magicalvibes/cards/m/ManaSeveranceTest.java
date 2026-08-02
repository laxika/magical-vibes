package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ManaSeveranceTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles every land the controller chooses, leaving nonlands in the library")
    void exilesChosenLands() {
        setupAndCast();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        chooseCard(0);
        chooseCard(0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");
        assertThat(gd.exiledCards.stream().map(ExiledCardEntry::card).map(Card::getName))
                .containsExactlyInAnyOrder("Plains", "Swamp");
        harness.assertInGraveyard(player1, "Mana Severance");
    }

    @Test
    @DisplayName("Only land cards are offered, including on the repeated picks")
    void offersOnlyLands() {
        setupAndCast();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactlyInAnyOrder("Plains", "Swamp");

        chooseCard(0);

        search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Swamp");
    }

    @Test
    @DisplayName("The controller may stop after any number of lands (fail to find)")
    void mayStopEarly() {
        setupAndCast();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        chooseCard(0);
        chooseCard(-1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.exiledCards.stream().map(ExiledCardEntry::card).map(Card::getName))
                .containsExactly("Plains");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Swamp", "Grizzly Bears");
    }

    @Test
    @DisplayName("A library with no lands asks for nothing and exiles nothing")
    void noLandsInLibrary() {
        harness.setHand(player1, List.of(new ManaSeverance()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.add(new GrizzlyBears());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.exiledCards).isEmpty();
        assertThat(deck).hasSize(1);
    }

    private void chooseCard(int index) {
        harness.getGameService().handleInteractionAnswer(harness.getGameData(), player1,
                new InteractionAnswer.LibraryCardChosen(index));
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new ManaSeverance()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0, 0);

        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Swamp(), new GrizzlyBears()));
    }
}
