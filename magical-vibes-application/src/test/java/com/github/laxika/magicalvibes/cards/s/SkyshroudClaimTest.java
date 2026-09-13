package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkyshroudClaim.class, Forest.class, Island.class, GrizzlyBears.class})
class SkyshroudClaimTest extends BaseCardTest {

    @Test
    @DisplayName("Offers up to two Forest cards and puts chosen cards onto the battlefield")
    void searchesForUpToTwoForests() {
        castWithLibrary(List.of(new Forest(), new Island(), new Forest(), new GrizzlyBears()));

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactly("Forest", "Forest");
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(p -> p.getCard().getName())
                .containsExactly("Forest", "Forest");
        assertThat(gd.playerBattlefields.get(player1.getId())).allMatch(p -> !p.isTapped());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("May find fewer than two Forest cards")
    void mayFindFewerThanTwoForests() {
        castWithLibrary(List.of(new Forest(), new Island(), new GrizzlyBears()));

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(p -> p.getCard().getName())
                .containsExactly("Forest");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Island", "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("May choose fewer than two Forest cards when two are available")
    void mayChooseFewerThanTwoForests() {
        Forest firstForest = new Forest();
        Forest secondForest = new Forest();
        Island island = new Island();
        castWithLibrary(List.of(firstForest, island, secondForest));

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(p -> p.getCard())
                .containsExactly(firstForest);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(island, secondForest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Does nothing when the library has no Forest cards")
    void doesNothingWithoutForests() {
        Island island = new Island();
        GrizzlyBears bears = new GrizzlyBears();
        castWithLibrary(List.of(island, bears));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(island, bears);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castWithLibrary(List<Card> library) {
        harness.setHand(player1, List.of(new SkyshroudClaim()));
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
