package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DiscoverTheFormula.class, Divination.class, Forest.class, Island.class})
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

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(inHand, soughtOne, soughtTwo, soughtThree);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, island);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, gd.playerHands.get(player1.getId()).indexOf(soughtOne));
    }
}
