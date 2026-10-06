package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GoblinTrailblazer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RiverwiseAugur.class, GoblinTrailblazer.class})
class RiverwiseAugurTest extends BaseCardTest {

    @Test
    void entersAndPutsTwoChosenCardsOnTopInOrder() {
        Card first = new GoblinTrailblazer();
        Card second = new GoblinTrailblazer();
        Card third = new GoblinTrailblazer();
        Card fourth = new GoblinTrailblazer();
        Card fifth = new GoblinTrailblazer();
        harness.setLibrary(player1, List.of(first, second, third, fourth, fifth));
        harness.setHand(player1, List.of(new RiverwiseAugur()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second, third);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second, fourth, fifth);
    }

    @Test
    void canReturnCardsAlreadyInHandInChosenOrder() {
        Card heldFirst = new GoblinTrailblazer();
        Card heldSecond = new GoblinTrailblazer();
        Card firstDraw = new GoblinTrailblazer();
        Card secondDraw = new GoblinTrailblazer();
        Card thirdDraw = new GoblinTrailblazer();
        Card remaining = new GoblinTrailblazer();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, thirdDraw, remaining));
        harness.setHand(player1, List.of(new RiverwiseAugur(), heldFirst, heldSecond));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(heldFirst, heldSecond, firstDraw, secondDraw, thirdDraw);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(heldFirst.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(heldFirst.getId(), heldSecond.getId(), firstDraw.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(heldSecond.getId(), heldFirst.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw, thirdDraw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(heldSecond, heldFirst, remaining);
    }
}
