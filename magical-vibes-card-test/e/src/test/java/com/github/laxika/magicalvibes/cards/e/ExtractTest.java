package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.PsychogenicProbe;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Extract.class, Forest.class, Island.class, PsychogenicProbe.class})
class ExtractTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles one chosen card from the target player's library and shuffles")
    void exilesOneCardFromTargetLibrary() {
        Card island = new Island();
        Card forest = new Forest();
        harness.setLibrary(player2, List.of(island, forest));

        harness.setHand(player1, List.of(new Extract()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .hasSize(2);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(island);
        assertThat(gd.findExiledCard(island.getId()).faceDown()).isFalse();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertInGraveyard(player1, "Extract");
    }

    @Test
    @DisplayName("An empty target library produces no search interaction")
    void emptyTargetLibraryProducesNoSearchInteraction() {
        harness.setLibrary(player2, List.of());

        harness.setHand(player1, List.of(new Extract()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertInGraveyard(player1, "Extract");
    }

    @Test
    @DisplayName("Can target its controller's library")
    void canTargetItsControllersLibrary() {
        Card island = new Island();
        harness.setLibrary(player1, List.of(island));

        harness.setHand(player1, List.of(new Extract()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(island);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("An empty target library is still shuffled")
    void emptyTargetLibraryStillShuffles() {
        harness.addToBattlefield(player1, new PsychogenicProbe());
        harness.setLibrary(player2, List.of());
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Extract()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Cannot decline to find a card in a nonempty library")
    void cannotDeclineToFindInNonemptyLibrary() {
        Card forest = new Forest();
        harness.setLibrary(player2, List.of(forest));
        harness.setHand(player1, List.of(new Extract()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(forest);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(forest);
    }

    @Test
    @DisplayName("A successful search shuffles only the targeted player's library")
    void successfulSearchShufflesTargetLibrary() {
        harness.addToBattlefield(player1, new PsychogenicProbe());
        Card forest = new Forest();
        Card island = new Island();
        harness.setLibrary(player2, List.of(forest, island));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Extract()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(island);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(forest);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }
}
