package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ImpulseTest extends BaseCardTest {

    private Card[] stackFourOnTop() {
        Card top1 = new GrizzlyBears();
        Card top2 = new LlanowarElves();
        Card top3 = new Shock();
        Card top4 = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(top1, top2, top3, top4));
        return new Card[]{top1, top2, top3, top4};
    }

    @Test
    @DisplayName("Looks at top four; chosen card to hand, rest reordered onto the bottom")
    void chosenCardToHandRestOnBottom() {
        harness.setHand(player1, List.of(new Impulse()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        Card[] top = stackFourOnTop();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(top[0].getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(top[0]);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(top[1], top[2], top[3]);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        List<Card> reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        assertThat(reorder).containsExactlyInAnyOrder(top[1], top[2], top[3]);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).toBottom()).isTrue();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(
                List.of(reorder.indexOf(top[1]), reorder.indexOf(top[2]), reorder.indexOf(top[3]))));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top[1], top[2], top[3]);
        harness.assertInGraveyard(player1, "Impulse");
    }

    @Test
    @DisplayName("With two cards, chosen goes to hand and the other to bottom with no reorder")
    void worksWithSmallLibrary() {
        Card c0 = new GrizzlyBears();
        Card c1 = new Shock();
        harness.setLibrary(player1, List.of(c0, c1));
        harness.setHand(player1, List.of(new Impulse()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(c0.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(c0);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(c1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
