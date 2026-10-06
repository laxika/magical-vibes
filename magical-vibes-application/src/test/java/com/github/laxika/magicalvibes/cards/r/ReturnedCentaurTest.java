package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.u.UnholyHunger;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReturnedCentaur.class, UnholyHunger.class})
class ReturnedCentaurTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving puts Returned Centaur on the battlefield with its ETB trigger on the stack")
    void resolvingPutsOnBattlefieldWithEtbOnStack() {
        castReturnedCentaur(player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Returned Centaur");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("ETB trigger mills four cards from the target player's library")
    void etbMillsFourCards() {
        trimDeck(player2.getId(), 10);

        castReturnedCentaur(player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(6);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Can target yourself to mill your own library")
    void canTargetSelf() {
        trimDeck(player1.getId(), 10);

        castReturnedCentaur(player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Mills only the remaining cards when the library has fewer than four")
    void millsOnlyRemainingWhenLibrarySmall() {
        trimDeck(player2.getId(), 2);

        castReturnedCentaur(player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Mills nothing when the library is empty")
    void millsNothingWhenLibraryEmpty() {
        gd.playerDecks.get(player2.getId()).clear();

        castReturnedCentaur(player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mills the top four cards and leaves the other player's library unchanged")
    void millsTopFourOnly() {
        List<Card> library = List.of(new ReturnedCentaur(), new ReturnedCentaur(),
                new ReturnedCentaur(), new ReturnedCentaur(), new ReturnedCentaur());
        harness.setLibrary(player2, library);
        List<Card> controllerLibrary = List.copyOf(gd.playerDecks.get(player1.getId()));

        castReturnedCentaur(player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactlyInAnyOrderElementsOf(library.subList(0, 4));
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(library.get(4));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(controllerLibrary);
    }

    @Test
    @DisplayName("The mill trigger resolves even after Returned Centaur is destroyed")
    void triggerResolvesAfterSourceIsDestroyed() {
        trimDeck(player2.getId(), 10);
        castReturnedCentaur(player2.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new UnholyHunger()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castInstant(player1, 0, findPermanent(player1, "Returned Centaur").getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Returned Centaur");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(10);

        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(6);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
    }

    private void trimDeck(UUID playerId, int size) {
        List<Card> deck = gd.playerDecks.get(playerId);
        while (deck.size() > size) {
            deck.removeFirst();
        }
    }

    private void castReturnedCentaur(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new ReturnedCentaur()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0, targetPlayerId);
    }
}
