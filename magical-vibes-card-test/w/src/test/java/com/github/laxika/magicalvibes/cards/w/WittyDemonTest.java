package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WittyDemon.class})
class WittyDemonTest extends BaseCardTest {

    @Test
    void searchesWhenStartingDeckHasThirteenCardsOverMinimum() {
        Card target = new WittyDemon();
        gd.startingDeckSizes.put(player1.getId(), 53);
        harness.setLibrary(player1, List.of(target));

        harness.enterBattlefieldAndReturn(player1, new WittyDemon());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(target);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void dealsDamageWhenStartingDeckIsNotLargeEnough() {
        gd.startingDeckSizes.put(player1.getId(), 52);

        harness.enterBattlefieldAndReturn(player1, new WittyDemon());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
    }

    @Test
    void emptyLibraryDoesNotTurnSuccessfulDeckConditionIntoDamage() {
        gd.startingDeckSizes.put(player1.getId(), 53);
        harness.setLibrary(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new WittyDemon());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void damageUsesControllerStartingDeckRatherThanOpponents() {
        gd.startingDeckSizes.put(player1.getId(), 53);
        gd.startingDeckSizes.put(player2.getId(), 52);

        harness.enterBattlefieldAndReturn(player2, new WittyDemon());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 16);
    }

    @Test
    void constructedDeckBelowSeventyThreeCardsDealsDamage() {
        gd.format = DeckFormat.MODERN;
        gd.startingDeckSizes.put(player1.getId(), 72);
        harness.setLibrary(player1, List.of(new WittyDemon()));

        harness.enterBattlefieldAndReturn(player1, new WittyDemon());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertLife(player1, 16);
    }

    @Test
    void constructedDeckWithThirteenExtraCardsSearches() {
        gd.format = DeckFormat.MODERN;
        gd.startingDeckSizes.put(player1.getId(), 73);
        Card target = new WittyDemon();
        harness.setLibrary(player1, List.of(target));

        harness.enterBattlefieldAndReturn(player1, new WittyDemon());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(target);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(target);
        harness.assertLife(player1, 20);
    }
}
