package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BayFalcon;
import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.StoneRain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MysticalTutor.class, Boomerang.class, StoneRain.class, BayFalcon.class, Island.class})
class MysticalTutorTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving offers only instant and sorcery cards")
    void offersOnlyInstantsAndSorceries() {
        List<Card> deck = setupLibrary();
        cast();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactlyInAnyOrder(deck.get(0), deck.get(1));
    }

    @Test
    @DisplayName("Choosing a card puts it on top of the library")
    void choosingCardPutsOnTop() {
        List<Card> deck = setupLibrary();
        cast();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        Card chosen = offered.getFirst();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(deck);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("reveals")).isTrue();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    @Test
    @DisplayName("Search may fail to find when no instant or sorcery is present")
    void mayFailToFind() {
        List<Card> deck = List.of(new BayFalcon(), new Island());
        harness.setLibrary(player1, deck);
        cast();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(deck);
    }

    @Test
    @DisplayName("The controller may choose not to find a matching card")
    void canChooseNotToFind() {
        List<Card> deck = setupLibrary();
        cast();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(deck);
    }

    private void cast() {
        harness.castFromHand(player1, new MysticalTutor(), "{U}");
    }

    private List<Card> setupLibrary() {
        List<Card> deck = List.of(new Boomerang(), new StoneRain(), new BayFalcon(), new Island());
        harness.setLibrary(player1, deck);
        return deck;
    }
}
