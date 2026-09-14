package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PortInspector;
import com.github.laxika.magicalvibes.cards.r.RishadanPort;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HiredGiant.class, Forest.class, RishadanPort.class, PortInspector.class})
class HiredGiantTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers each opponent a land search, not the controller")
    void etbOffersOpponentLandSearch() {
        harness.setLibrary(player2, List.of(new RishadanPort(), new PortInspector(), new Forest()));
        castHiredGiant();
        resolveEtb();

        PendingInteraction.LibrarySearch search = activeSearch();
        assertThat(search).isNotNull();
        assertThat(search.params().playerId()).isEqualTo(player2.getId());
        assertThat(search.params().cards()).hasSize(2);
        assertThat(search.params().cards()).allMatch(card -> card.hasType(CardType.LAND));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    @Test
    @DisplayName("Opponent may put a nonbasic land onto the battlefield untapped")
    void opponentMayPutNonbasicLandUntapped() {
        RishadanPort land = new RishadanPort();
        harness.setLibrary(player2, List.of(land, new PortInspector()));
        castHiredGiant();
        resolveEtb();

        PendingInteraction.LibrarySearch search = activeSearch();
        int landIndex = indexOf(search, RishadanPort.class);
        harness.handleCardChosen(player2, landIndex);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() == land && !permanent.isTapped());
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(land);
        assertThat(activeSearch()).isNull();
    }

    @Test
    @DisplayName("Opponent may decline the land search")
    void opponentMayDecline() {
        harness.setLibrary(player2, List.of(new Forest()));
        castHiredGiant();
        resolveEtb();

        int before = gd.playerBattlefields.get(player2.getId()).size();
        harness.handleCardChosen(player2, -1);

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(before);
        assertThat(activeSearch()).isNull();
    }

    @Test
    @DisplayName("No land search is offered when an opponent has no land cards")
    void noLandNoPrompt() {
        harness.setLibrary(player2, List.of(new PortInspector()));
        castHiredGiant();
        resolveEtb();

        assertThat(activeSearch()).isNull();
    }

    private void castHiredGiant() {
        harness.castFromHand(player1, new HiredGiant(), "{3}{R}");
    }

    private void resolveEtb() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private PendingInteraction.LibrarySearch activeSearch() {
        return harness.getGameData().interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }

    private int indexOf(PendingInteraction.LibrarySearch search, Class<? extends Card> cardType) {
        for (int i = 0; i < search.params().cards().size(); i++) {
            if (cardType.isInstance(search.params().cards().get(i))) {
                return i;
            }
        }
        throw new IllegalStateException("No matching card in search options");
    }

}
