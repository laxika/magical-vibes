package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

class SelectiveMemoryTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles every nonland card the controller chooses")
    void exilesChosenNonlands() {
        setupAndCast();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        chooseCard(0);
        chooseCard(0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest");
        assertThat(gd.exiledCards.stream().map(ExiledCardEntry::card).map(Card::getName))
                .containsExactlyInAnyOrder("Grizzly Bears", "Grizzly Bears");
        harness.assertInGraveyard(player1, "Selective Memory");
    }

    @Test
    @DisplayName("Only nonland cards are offered on repeated picks")
    void offersOnlyNonlands() {
        setupAndCast();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactly("Grizzly Bears", "Grizzly Bears");

        chooseCard(0);

        search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("The controller may stop after any number of nonland cards")
    void mayStopEarly() {
        setupAndCast();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        chooseCard(-1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getName().equals("Grizzly Bears"));
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Grizzly Bears", "Grizzly Bears");
    }

    private void chooseCard(int index) {
        harness.getGameService().handleInteractionAnswer(harness.getGameData(), player1,
                new InteractionAnswer.LibraryCardChosen(index));
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new SelectiveMemory()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castSorcery(player1, 0, 0);

        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new GrizzlyBears(), new Forest(), new GrizzlyBears()));
    }
}
