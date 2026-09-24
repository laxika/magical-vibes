package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.WorldlyCounsel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ManipulateFate.class, Forest.class, Island.class, Plains.class, WorldlyCounsel.class})
class ManipulateFateTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles three cards, shuffles, then draws a card")
    void exilesThreeThenDraws() {
        Card exiledOne = new Forest();
        Card exiledTwo = new Island();
        Card exiledThree = new Plains();
        Card drawn = new WorldlyCounsel();
        harness.setLibrary(player1, List.of(exiledOne, exiledTwo, exiledThree, drawn));

        harness.castFromHand(player1, new ManipulateFate(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(exiledOne.getId(), exiledTwo.getId(), exiledThree.getId());
        assertThat(gd.exiledCards).noneMatch(com.github.laxika.magicalvibes.model.ExiledCardEntry::faceDown);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).containsExactly(drawn.getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Exiles all available cards when the library has fewer than three")
    void exilesAllAvailableCardsWhenLibraryIsShort() {
        Card exiledOne = new Forest();
        Card exiledTwo = new Island();
        harness.setLibrary(player1, List.of(exiledOne, exiledTwo));

        harness.castFromHand(player1, new ManipulateFate(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(exiledOne.getId(), exiledTwo.getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }
}
