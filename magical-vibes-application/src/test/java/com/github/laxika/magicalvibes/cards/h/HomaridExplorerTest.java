package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HomaridExplorer.class})
class HomaridExplorerTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving puts Homarid Explorer on battlefield with ETB trigger on stack")
    void resolvingPutsOnBattlefieldWithEtbOnStack() {
        castHomaridExplorer(player2.getId());
        harness.passBothPriorities(); // resolve creature spell

        harness.assertOnBattlefield(player1, "Homarid Explorer");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("ETB trigger mills four cards from target player's library")
    void etbMillsFourCards() {
        List<Card> deck = gd.playerDecks.get(player2.getId());
        harness.setLibrary(player2, deck.subList(deck.size() - 10, deck.size()));

        castHomaridExplorer(player2.getId());
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(6);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Can target yourself to mill your own library")
    void canTargetSelf() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        harness.setLibrary(player1, deck.subList(deck.size() - 10, deck.size()));

        castHomaridExplorer(player1.getId());
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Mills only remaining cards when library has fewer than four")
    void millsOnlyRemainingWhenLibrarySmall() {
        List<Card> deck = gd.playerDecks.get(player2.getId());
        harness.setLibrary(player2, deck.subList(deck.size() - 2, deck.size()));

        castHomaridExplorer(player2.getId());
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Mills nothing when library is empty")
    void millsNothingWhenLibraryEmpty() {
        gd.playerDecks.get(player2.getId()).clear();

        castHomaridExplorer(player2.getId());
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Stack is empty after full resolution")
    void stackIsEmptyAfterResolution() {
        castHomaridExplorer(player2.getId());
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only the top four cards are milled and the other player's library is unchanged")
    void millsTopFourCardsOnly() {
        List<Card> library = List.of(new HomaridExplorer(), new HomaridExplorer(),
                new HomaridExplorer(), new HomaridExplorer(), new HomaridExplorer());
        harness.setLibrary(player2, library);
        List<Card> controllerLibrary = List.copyOf(gd.playerDecks.get(player1.getId()));

        castHomaridExplorer(player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactlyInAnyOrderElementsOf(library.subList(0, 4));
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(library.get(4));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(controllerLibrary);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private void castHomaridExplorer(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new HomaridExplorer()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0, targetPlayerId);
    }
}
