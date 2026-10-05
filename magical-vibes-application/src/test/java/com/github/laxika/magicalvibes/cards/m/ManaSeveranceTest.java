package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AncientTomb;
import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.Recoup;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ManaSeverance.class, Plains.class, Swamp.class, Counterspell.class, AncientTomb.class, Recoup.class})
class ManaSeveranceTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles every land the controller chooses, leaving nonlands in the library")
    void exilesChosenLands() {
        setupAndCast();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Counterspell");
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

        harness.handleCardChosen(player1, 0);

        search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Swamp");
    }

    @Test
    @DisplayName("The controller may choose zero lands")
    void mayChooseZeroLands() {
        setupAndCast();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Plains", "Swamp", "Counterspell");
        harness.assertInGraveyard(player1, "Mana Severance");
    }

    @Test
    @DisplayName("The controller may stop after any number of lands (fail to find)")
    void mayStopEarly() {
        setupAndCast();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.exiledCards.stream().map(ExiledCardEntry::card).map(Card::getName))
                .containsExactly("Plains");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Swamp", "Counterspell");
    }

    @Test
    @DisplayName("A library with no lands asks for nothing and exiles nothing")
    void noLandsInLibrary() {
        harness.castFromHand(player1, new ManaSeverance(), "{1}{U}");

        harness.setLibrary(player1, List.of(new Counterspell()));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An empty library finishes without a choice")
    void emptyLibrary() {
        harness.castFromHand(player1, new ManaSeverance(), "{1}{U}");
        harness.setLibrary(player1, List.of());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).isEmpty();
        harness.assertInGraveyard(player1, "Mana Severance");
    }

    @Test
    @DisplayName("Nonbasic lands can be exiled and the opponent's library is untouched")
    void exilesNonbasicLandOnlyFromOwnLibrary() {
        AncientTomb ownLand = new AncientTomb();
        AncientTomb opposingLand = new AncientTomb();
        harness.setLibrary(player1, List.of(ownLand));
        harness.setLibrary(player2, List.of(opposingLand));
        harness.castFromHand(player1, new ManaSeverance(), "{1}{U}");

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opposingLand);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(ownLand);
        harness.assertInGraveyard(player1, "Mana Severance");
    }

    @Test
    @DisplayName("Granted flashback still allows choosing multiple lands")
    void flashbackDoesNotLimitNumberOfLands() {
        ManaSeverance severance = new ManaSeverance();
        harness.setGraveyard(player1, List.of(severance));
        harness.setHand(player1, List.of(new Recoup()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0, severance.getId());
        harness.passBothPriorities();
        harness.setLibrary(player1, List.of(new Plains(), new Swamp()));

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Plains", "Swamp", "Mana Severance");
    }
    private void setupAndCast() {
        harness.castFromHand(player1, new ManaSeverance(), "{1}{U}");

        harness.setLibrary(player1, List.of(new Plains(), new Swamp(), new Counterspell()));
    }
}
