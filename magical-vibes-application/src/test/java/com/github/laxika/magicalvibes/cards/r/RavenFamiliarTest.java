package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RavenFamiliarTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts one of the top three cards into hand and bottoms the rest in order")
    void etbPicksOneAndBottomOrdersTheRest() {
        Card top = new GrizzlyBears();
        Card second = new LlanowarElves();
        Card third = new GrizzlyBears();
        Card untouched = new LlanowarElves();
        harness.setLibrary(player1, List.of(top, second, third, untouched));
        harness.setHand(player1, List.of(new RavenFamiliar()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validCardIds()).containsExactly(top.getId(), second.getId(), third.getId());

        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        PendingInteraction.LibraryReorder reorder = gd.interaction
                .activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder.cards()).containsExactly(top, third);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).contains(second);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(top, third, untouched);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, third, top);
    }

    @Test
    @DisplayName("ETB takes the only available card when the library has fewer than three")
    void etbUsesAvailableCardsOnly() {
        Card only = new GrizzlyBears();
        harness.setLibrary(player1, List.of(only));
        harness.setHand(player1, List.of(new RavenFamiliar()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(only);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
