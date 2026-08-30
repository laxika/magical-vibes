package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VerdantMasteryTest extends BaseCardTest {

    @Test
    @DisplayName("Normal casting puts two chosen basics onto the controller's battlefield and the rest into hand")
    void normalCastDistributesChosenLands() {
        Card forest = new Forest();
        Card island = new Island();
        Card mountain = new Mountain();
        Card plains = new Plains();
        cast(List.of(forest, island, mountain, plains), false);

        harness.handleMultipleCardsChosen(player1,
                List.of(forest.getId(), island.getId(), mountain.getId(), plains.getId()));
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.VerdantMasteryLandChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(forest.getId(), island.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(forest.getId(), island.getId())
                .doesNotContain(mountain.getId(), plains.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getId().equals(forest.getId())
                        || permanent.getCard().getId().equals(island.getId()))
                .allMatch(permanent -> permanent.isTapped());
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(mountain.getId(), plains.getId());
    }

    @Test
    @DisplayName("Alternate casting gives one chosen basic to an opponent and two to the controller")
    void alternateCastGivesOpponentOneLand() {
        Card forest = new Forest();
        Card island = new Island();
        Card mountain = new Mountain();
        Card plains = new Plains();
        cast(List.of(forest, island, mountain, plains), true);

        harness.handleMultipleCardsChosen(player1,
                List.of(forest.getId(), island.getId(), mountain.getId(), plains.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.VerdantMasteryLandChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(island.getId(), mountain.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(forest.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(island.getId(), mountain.getId());
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(plains.getId());
    }

    @Test
    @DisplayName("Declining the search finds no lands and still shuffles")
    void canFindNone() {
        Card forest = new Forest();
        cast(List.of(forest), false);

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).contains(forest);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                permanent -> permanent.getCard().getId().equals(forest.getId()));
    }

    private void cast(List<Card> library, boolean alternate) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new VerdantMastery()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, alternate ? 3 : 5);
        if (alternate) {
            harness.castWithAlternateCost(player1, 0, List.of());
        } else {
            harness.castSorcery(player1, 0, 0);
        }
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.VerdantMasterySearchChoice.class);
    }
}
