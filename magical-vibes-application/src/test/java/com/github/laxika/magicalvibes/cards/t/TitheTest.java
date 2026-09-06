package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Tithe.class, Forest.class, Plains.class, GrizzlyBears.class})
class TitheTest extends BaseCardTest {

    @Test
    @DisplayName("When opponent has more lands, search allows up to two Plains")
    void opponentHasMoreLandsAllowsTwoPlains() {
        setupAndCast();
        harness.addToBattlefield(player2, new Forest());
        List<Card> library = setupLibrary(2);

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(2);
        assertThat(search.params().cards()).containsExactly(library.get(0), library.get(1));
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(library.get(0), library.get(1));
    }

    @Test
    @DisplayName("When opponent has more lands, the additional Plains search may be declined")
    void mayDeclineAdditionalPlains() {
        setupAndCast();
        harness.addToBattlefield(player2, new Forest());
        List<Card> library = setupLibrary(2);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        PendingInteraction.LibrarySearch additionalSearch =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(additionalSearch).isNotNull();
        assertThat(additionalSearch.params().remainingCount()).isEqualTo(1);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(library.get(0));
    }

    @Test
    @DisplayName("When land counts are equal, search allows only one Plains")
    void equalLandsAllowsOnlyOnePlains() {
        setupAndCast();
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        List<Card> library = setupLibrary(2);

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().remainingCount()).isEqualTo(1);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(library.get(0));
    }

    @Test
    @DisplayName("Land count is checked on resolution, not announcement")
    void landCountCheckedOnResolution() {
        setupAndCast();
        setupLibrary(2);

        // Equal at announcement; opponent gains a land before resolution.
        harness.addToBattlefield(player2, new Forest());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().remainingCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new Tithe()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new Tithe()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, player2.getId());
    }

    private List<Card> setupLibrary(int plainsCount) {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < plainsCount; i++) {
            deck.add(new Plains());
        }
        deck.add(new GrizzlyBears());
        harness.setLibrary(player1, deck);
        return deck;
    }
}
