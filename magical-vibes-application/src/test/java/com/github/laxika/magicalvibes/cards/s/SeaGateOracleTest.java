package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SeaGateOracleTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts the chosen card into hand and the other on the bottom")
    void etbChoosesOneForHand() {
        Card top = new GrizzlyBears();
        Card second = new LlanowarElves();
        harness.setLibrary(player1, List.of(top, second));
        harness.setHand(player1, List.of(new SeaGateOracle()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(second).doesNotContain(top);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    @DisplayName("ETB can choose the top card instead")
    void etbCanChooseTopCard() {
        Card top = new GrizzlyBears();
        Card second = new LlanowarElves();
        harness.setLibrary(player1, List.of(top, second));
        harness.setHand(player1, List.of(new SeaGateOracle()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(top.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(top).doesNotContain(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
    }
}
