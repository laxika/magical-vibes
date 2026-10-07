package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NervousGardener;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({TheyWentThisWay.class, Forest.class, NervousGardener.class})
class TheyWentThisWayTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for a basic land tapped and investigates")
    void searchesForBasicLandAndInvestigates() {
        Forest forest = new Forest();
        setupAndResolve(List.of(forest, new NervousGardener()));

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == forest && permanent.isTapped());
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(forest).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Investigates even when no basic land is found")
    void investigatesWithoutBasicLand() {
        setupAndResolve(List.of(new NervousGardener()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().hasType(CardType.LAND)
                        && permanent.getCard().getSupertypes().contains(CardSupertype.BASIC));
    }

    @Test
    @DisplayName("May fail to find a basic land that is present and still investigates")
    void mayDeclineToFindBasicLand() {
        Forest forest = new Forest();
        setupAndResolve(List.of(forest));

        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        harness.assertInGraveyard(player1, "They Went This Way");
    }

    @Test
    @DisplayName("Investigates even with an empty library")
    void investigatesWithEmptyLibrary() {
        setupAndResolve(List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        harness.assertInGraveyard(player1, "They Went This Way");
    }

    @Test
    @DisplayName("The created Clue can be sacrificed for two mana to draw a card")
    void clueCanBeSacrificedToDraw() {
        NervousGardener cardToDraw = new NervousGardener();
        setupAndResolve(List.of(cardToDraw));
        int clueIndex = gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Clue"));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, clueIndex, null, null);

        harness.assertNotOnBattlefield(player1, "Clue");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(cardToDraw);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void setupAndResolve(List<Card> library) {
        harness.setHand(player1, List.of(new TheyWentThisWay()));
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
