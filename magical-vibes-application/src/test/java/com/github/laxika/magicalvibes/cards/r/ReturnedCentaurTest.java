package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(6);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Can target yourself to mill your own library")
    void canTargetSelf() {
        trimDeck(player1.getId(), 10);

        castReturnedCentaur(player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Mills only the remaining cards when the library has fewer than four")
    void millsOnlyRemainingWhenLibrarySmall() {
        trimDeck(player2.getId(), 2);

        castReturnedCentaur(player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Mills nothing when the library is empty")
    void millsNothingWhenLibraryEmpty() {
        gd.playerDecks.get(player2.getId()).clear();

        castReturnedCentaur(player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
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
        harness.getGameService().playCard(gd, player1, 0, 0, targetPlayerId, null);
    }
}
