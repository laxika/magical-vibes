package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeeBeyond.class, Forest.class, Island.class, Plains.class})
class SeeBeyondTest extends BaseCardTest {

    @Test
    void drawsTwoCardsThenShufflesOneFromHandIntoLibrary() {
        Card shuffled = new Forest();
        Card kept = new Island();
        harness.setLibrary(player1, List.of(shuffled, kept, new Plains()));
        harness.setHand(player1, List.of(new SeeBeyond()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(shuffled.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2).contains(shuffled);
        harness.assertInGraveyard(player1, "See Beyond");
    }

    @Test
    void canShuffleACardAlreadyInHandAfterDrawingTheLastTwoLibraryCards() {
        Card shuffled = new Forest();
        Card firstDraw = new Island();
        Card secondDraw = new Plains();
        harness.setHand(player1, List.of(new SeeBeyond(), shuffled));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(shuffled, firstDraw, secondDraw);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();

        harness.handleMultipleCardsChosen(player1, List.of(shuffled.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shuffled);
        harness.assertInGraveyard(player1, "See Beyond");
    }

    @Test
    void mustChooseExactlyOneCardToShuffle() {
        Card firstDraw = new Forest();
        Card secondDraw = new Island();
        harness.setHand(player1, List.of(new SeeBeyond()));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, new Plains()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(firstDraw.getId(), secondDraw.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);

        harness.handleMultipleCardsChosen(player1, List.of(firstDraw.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondDraw);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2).contains(firstDraw);
        harness.assertInGraveyard(player1, "See Beyond");
    }
}
