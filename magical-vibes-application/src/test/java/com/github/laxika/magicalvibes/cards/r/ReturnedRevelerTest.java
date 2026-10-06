package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReturnedReveler.class, Island.class})
class ReturnedRevelerTest extends BaseCardTest {

    @Test
    @DisplayName("When it dies, each player mills three cards")
    void eachPlayerMillsThreeCardsWhenItDies() {
        List<Card> player1Library = List.of(new Island(), new Island(), new Island(), new Island());
        List<Card> player2Library = List.of(new Island(), new Island(), new Island(), new Island());
        harness.setLibrary(player1, player1Library);
        harness.setLibrary(player2, player2Library);
        Permanent reveler = harness.addToBattlefieldAndReturn(player1, new ReturnedReveler());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, reveler));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(player1Library.get(3));
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(player2Library.get(3));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(reveler.getCard(), player1Library.get(0), player1Library.get(1), player1Library.get(2));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactly(player2Library.get(0), player2Library.get(1), player2Library.get(2));
    }

    @Test
    @DisplayName("A short library mills only its remaining cards without preventing the other player's mill")
    void shortLibraryDoesNotPreventOtherPlayerFromMilling() {
        List<Card> player1Library = List.of(new ReturnedReveler(), new ReturnedReveler());
        List<Card> player2Library = List.of(new ReturnedReveler(), new ReturnedReveler(),
                new ReturnedReveler(), new ReturnedReveler());
        harness.setLibrary(player1, player1Library);
        harness.setLibrary(player2, player2Library);
        Permanent reveler = harness.addToBattlefieldAndReturn(player1, new ReturnedReveler());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, reveler));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(player1Library);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(player2Library);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(player2Library.get(3));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(reveler.getCard(), player1Library.get(0), player1Library.get(1));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactly(player2Library.get(0), player2Library.get(1), player2Library.get(2));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An empty library does not prevent the other player from milling")
    void emptyLibraryDoesNotPreventOtherPlayerFromMilling() {
        List<Card> player1Library = List.of(new ReturnedReveler(), new ReturnedReveler(), new ReturnedReveler());
        harness.setLibrary(player1, player1Library);
        harness.setLibrary(player2, List.of());
        Permanent reveler = harness.addToBattlefieldAndReturn(player2, new ReturnedReveler());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, reveler));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(player1Library);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(reveler.getCard());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Exiling Returned Reveler does not trigger milling")
    void exileDoesNotTriggerMilling() {
        List<Card> player1Library = List.of(new ReturnedReveler(), new ReturnedReveler(), new ReturnedReveler());
        List<Card> player2Library = List.of(new ReturnedReveler(), new ReturnedReveler(), new ReturnedReveler());
        harness.setLibrary(player1, player1Library);
        harness.setLibrary(player2, player2Library);
        Permanent reveler = harness.addToBattlefieldAndReturn(player1, new ReturnedReveler());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, reveler));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(player1Library);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(player2Library);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(entry -> entry.card()).containsExactly(reveler.getCard());
    }
}
