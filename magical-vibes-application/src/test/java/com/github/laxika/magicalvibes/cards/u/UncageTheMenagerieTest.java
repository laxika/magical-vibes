package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AmbushViper;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UncageTheMenagerieTest extends BaseCardTest {

    @Test
    @DisplayName("Offers only creatures with mana value exactly X")
    void offersOnlyCreaturesWithManaValueX() {
        castUncage(2);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        // Grizzly Bears, Ambush Viper, Runeclaw Bear are MV 2; Llanowar Elves (1), Air Elemental (5), Shock excluded.
        assertThat(offeredNames(gd)).containsExactlyInAnyOrder("Grizzly Bears", "Ambush Viper", "Runeclaw Bear");
        assertThat(activeSearch().params().destination()).isEqualTo(LibrarySearchDestination.HAND);
        assertThat(activeSearch().params().remainingCount()).isEqualTo(2);
        assertThat(activeSearch().params().requireDifferentNames()).isTrue();
    }

    @Test
    @DisplayName("Choosing creatures puts them into hand and excludes same-named cards from further picks")
    void putsIntoHandAndRequiresDifferentNames() {
        castUncage(2);
        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new GrizzlyBears(), new GrizzlyBears(), new AmbushViper(), new RuneclawBear()));

        harness.passBothPriorities();

        int bearsIndex = indexOf(offeredNames(gd), "Grizzly Bears");
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(bearsIndex));

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(offeredNames(gd)).containsExactlyInAnyOrder("Ambush Viper", "Runeclaw Bear");
        assertThat(offeredNames(gd)).doesNotContain("Grizzly Bears");
        assertThat(activeSearch().params().remainingCount()).isEqualTo(1);

        int viperIndex = indexOf(offeredNames(gd), "Ambush Viper");
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(viperIndex));

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Ambush Viper"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("X=0 searches and shuffles without finding cards")
    void xZeroSearchesAndShuffles() {
        castUncage(0);
        setupLibrary();
        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getName().equals("Uncage the Menagerie"));
    }

    @Test
    @DisplayName("Player may fail to find")
    void failToFind() {
        castUncage(2);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castUncage(int xValue) {
        harness.setHand(player1, List.of(new UncageTheMenagerie()));
        harness.addMana(player1, ManaColor.GREEN, xValue + 2);
        harness.castSorcery(player1, 0, xValue);
    }

    private void setupLibrary() {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(
                new GrizzlyBears(),
                new AmbushViper(),
                new RuneclawBear(),
                new LlanowarElves(),
                new AirElemental(),
                new Shock()));
    }

    private PendingInteraction.LibrarySearch activeSearch() {
        return harness.getGameData().interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }

    private List<String> offeredNames(GameData gd) {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().stream().map(Card::getName).toList();
    }

    private int indexOf(List<String> names, String name) {
        int index = names.indexOf(name);
        assertThat(index).isGreaterThanOrEqualTo(0);
        return index;
    }
}
