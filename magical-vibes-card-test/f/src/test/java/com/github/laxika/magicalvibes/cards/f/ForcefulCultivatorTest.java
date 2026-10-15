package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BoseijuWhoEndures;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ForcefulCultivator.class, Forest.class, GrizzlyBears.class, BoseijuWhoEndures.class})
class ForcefulCultivatorTest extends BaseCardTest {

    @Test
    @DisplayName("A nonbasic land in hand also prevents the cost reduction")
    void nonbasicLandPreventsReduction() {
        harness.setHand(player1, List.of(new ForcefulCultivator(), new BoseijuWhoEndures()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("The search excludes nonbasic lands and searches only the controller's library")
    void searchesOnlyOwnBasicLands() {
        Forest ownForest = new Forest();
        Forest opponentForest = new Forest();
        BoseijuWhoEndures nonbasicLand = new BoseijuWhoEndures();
        harness.setHand(player1, List.of(new ForcefulCultivator()));
        harness.setLibrary(player1, List.of(nonbasicLand, ownForest));
        harness.setLibrary(player2, List.of(opponentForest));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(ownForest);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(ownForest);
                    assertThat(permanent.isTapped()).isTrue();
                });
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonbasicLand);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentForest);
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Pays the full cost when a land remains in hand")
    void paysFullCostWithLandInHand() {
        harness.setHand(player1, List.of(new ForcefulCultivator(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("The reduction does not remove either green mana requirement")
    void stillRequiresTwoGreenMana() {
        harness.setHand(player1, List.of(new ForcefulCultivator()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Opponent lands in hand do not prevent the reduction")
    void ignoresOpponentHandForCostReduction() {
        harness.setHand(player1, List.of(new ForcefulCultivator()));
        harness.setHand(player2, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The restricted search may fail to find an available basic land")
    void mayFailToFindBasicLand() {
        harness.setHand(player1, List.of(new ForcefulCultivator()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertOnBattlefield(player1, "Forceful Cultivator");
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An empty library does not prevent the creature from entering")
    void resolvesWithEmptyLibrary() {
        harness.setHand(player1, List.of(new ForcefulCultivator()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forceful Cultivator");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Costs two generic mana less with no land cards in hand")
    void costsLessWithNoLandCardsInHand() {
        harness.setHand(player1, List.of(new ForcefulCultivator(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Does not receive the cost reduction with a land card in hand")
    void doesNotCostLessWithLandCardInHand() {
        harness.setHand(player1, List.of(new ForcefulCultivator(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Enters a searched basic land tapped")
    void entersBasicLandTapped() {
        harness.setHand(player1, List.of(new ForcefulCultivator()));
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().cards())
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC));

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().hasType(CardType.LAND) && permanent.isTapped());
    }
}
