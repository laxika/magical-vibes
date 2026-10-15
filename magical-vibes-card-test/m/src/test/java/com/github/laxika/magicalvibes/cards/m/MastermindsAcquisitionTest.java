package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.w.WorldShaper;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MastermindsAcquisition.class, Forest.class, Island.class, WorldShaper.class})
class MastermindsAcquisitionTest extends BaseCardTest {

    @Test
    @DisplayName("The library mode searches for any card and puts the chosen card into hand")
    void libraryModeSearchesLibrary() {
        Card searched = new WorldShaper();
        harness.setLibrary(player1, List.of(new Forest(), searched));
        castWithMode(0);

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().sourceSideboard()).isFalse();
        assertThat(search.params().reveals()).isFalse();
        assertThat(search.params().canFailToFind()).isFalse();
        assertThat(search.params().cards()).hasSize(2);
        assertThat(search.params().cards()).contains(searched);

        harness.handleCardChosen(player1, search.params().cards().indexOf(searched));

        assertThat(gd.playerHands.get(player1.getId())).contains(searched);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(searched);
    }

    @Test
    @DisplayName("The outside-the-game mode offers all sideboard cards and puts the chosen card into hand")
    void outsideGameModeSearchesSideboard() {
        Card chosen = new Island();
        Card remaining = new Forest();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(chosen, remaining)));
        castWithMode(1);

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().sourceSideboard()).isTrue();
        assertThat(search.params().reveals()).isFalse();
        assertThat(search.params().cards()).containsExactly(chosen, remaining);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(remaining);
    }

    @Test
    @DisplayName("The library mode resolves normally with an empty library")
    void libraryModeWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        castWithMode(0);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof MastermindsAcquisition);
    }

    @Test
    @DisplayName("The outside-the-game mode cannot take an exiled card or an opponent's sideboard card")
    void outsideGameModeWithEmptySideboard() {
        Card exiled = new WorldShaper();
        gd.addToExile(player1.getId(), exiled);
        gd.playerSideboards.put(player1.getId(), new ArrayList<>());
        Card opponentsCard = new Island();
        gd.playerSideboards.put(player2.getId(), new ArrayList<>(List.of(opponentsCard)));
        castWithMode(1);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(exiled);
        assertThat(gd.playerSideboards.get(player2.getId())).containsExactly(opponentsCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The outside-the-game mode may leave the sideboard card unchosen")
    void outsideGameModeCanDecline() {
        Card outsideCard = new Forest();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(outsideCard)));
        castWithMode(1);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(outsideCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void castWithMode(int mode) {
        harness.setHand(player1, List.of(new MastermindsAcquisition()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castModalSorceryWithModes(player1, 0, 1, new int[]{mode}, List.of());
    }
}
