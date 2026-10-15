package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.v.ValMaroonedSurveyor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DiscoverTheFormula.class, Divination.class, Forest.class, Island.class, ValMaroonedSurveyor.class})
class DiscoverTheFormulaTest extends BaseCardTest {

    @Test
    void seeksThreeNonlandCardsAndReducesTheirPerpetualCosts() {
        Divination inHand = new Divination();
        Divination soughtOne = new Divination();
        Divination soughtTwo = new Divination();
        Divination soughtThree = new Divination();
        Forest forest = new Forest();
        Island island = new Island();

        harness.setHand(player1, List.of(new DiscoverTheFormula(), inHand));
        harness.setLibrary(player1, List.of(forest, soughtOne, island, soughtTwo, soughtThree));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(inHand, soughtOne, soughtTwo, soughtThree);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, island);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, gd.playerHands.get(player1.getId()).indexOf(soughtOne));
    }

    @Test
    void seeksAllAvailableNonlandsWithoutReorderingRemainingLibrary() {
        Divination sought = new Divination();
        Forest first = new Forest();
        Island second = new Island();
        Forest third = new Forest();
        harness.setLibrary(player1, List.of(first, sought, second, third));
        harness.castFromHand(player1, new DiscoverTheFormula(), "{4}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(sought);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second, third);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void reducesExistingHandCardsEvenWhenNoNonlandsCanBeSought() {
        Divination inHand = new Divination();
        Forest inHandLand = new Forest();
        Island libraryLand = new Island();
        harness.setHand(player1, List.of(new DiscoverTheFormula(), inHand, inHandLand));
        harness.setLibrary(player1, List.of(libraryLand));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(inHand, inHandLand);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryLand);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void repeatedResolutionsStackTheCostReduction() {
        Divination inHand = new Divination();
        harness.setHand(player1, List.of(new DiscoverTheFormula(), new DiscoverTheFormula(), inHand));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castAndResolveInstant(player1, 0);

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void seekingThreeCardsTriggersValOnce() {
        harness.addToBattlefield(player1, new ValMaroonedSurveyor());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(new Divination(), new Divination(), new Divination()));
        harness.castFromHand(player1, new DiscoverTheFormula(), "{4}{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void cardsAcquiredAfterResolutionDoNotReceiveTheReduction() {
        Divination laterCard = new Divination();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.castFromHand(player1, new DiscoverTheFormula(), "{4}{U}{U}");
        harness.passBothPriorities();

        harness.setHand(player1, List.of(laterCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card is not playable");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void costReductionDoesNotPayForColoredMana() {
        Divination inHand = new Divination();
        harness.setHand(player1, List.of(new DiscoverTheFormula(), inHand));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castAndResolveInstant(player1, 0);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card is not playable");
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }
}
