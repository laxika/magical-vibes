package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SleightOfHandTest extends BaseCardTest {

    @Test
    @DisplayName("Chosen card goes to hand; the other goes to the bottom of the library")
    void chosenCardToHandOtherToBottom() {
        harness.setHand(player1, List.of(new SleightOfHand()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        Card top1 = new GrizzlyBears();
        Card top2 = new LlanowarElves();
        gd.playerDecks.get(player1.getId()).add(0, top2);
        gd.playerDecks.get(player1.getId()).add(0, top1); // top1 is now the very top

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(top1.getId()));

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).contains(top1);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(top2);
        assertThat(gd.playerDecks.get(player1.getId()).getLast()).isSameAs(top2);
        harness.assertInGraveyard(player1, "Sleight of Hand");
    }

    @Test
    @DisplayName("Can keep the second card instead; the first goes to the bottom")
    void canKeepSecondCard() {
        harness.setHand(player1, List.of(new SleightOfHand()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        Card top1 = new GrizzlyBears();
        Card top2 = new LlanowarElves();
        gd.playerDecks.get(player1.getId()).add(0, top2);
        gd.playerDecks.get(player1.getId()).add(0, top1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(top2.getId()));

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).contains(top2);
        assertThat(gd.playerDecks.get(player1.getId()).getLast()).isSameAs(top1);
    }
}
