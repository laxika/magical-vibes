package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.CloudElemental;
import com.github.laxika.magicalvibes.cards.f.Foreshadow;
import com.github.laxika.magicalvibes.cards.j.JamuraanLion;
import com.github.laxika.magicalvibes.cards.w.Warthog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Impulse.class, CloudElemental.class, Foreshadow.class, JamuraanLion.class, Warthog.class})
class ImpulseTest extends BaseCardTest {

    private Card[] stackFourOnTop() {
        Card top1 = new CloudElemental();
        Card top2 = new Foreshadow();
        Card top3 = new JamuraanLion();
        Card top4 = new Warthog();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(top1, top2, top3, top4));
        return new Card[]{top1, top2, top3, top4};
    }

    @Test
    @DisplayName("Looks at top four; chosen card to hand, rest reordered onto the bottom")
    void chosenCardToHandRestOnBottom() {
        Card[] top = stackFourOnTop();
        harness.castFromHand(player1, new Impulse(), "{1}{U}");

        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(top[0].getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(top[0]);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(top[1], top[2], top[3]);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        List<Card> reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        assertThat(reorder).containsExactlyInAnyOrder(top[1], top[2], top[3]);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).toBottom()).isTrue();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(
                List.of(reorder.indexOf(top[3]), reorder.indexOf(top[2]), reorder.indexOf(top[1]))));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top[3], top[2], top[1]);
        harness.assertInGraveyard(player1, "Impulse");
    }

    @Test
    @DisplayName("With two cards, chosen goes to hand and the other to bottom with no reorder")
    void worksWithSmallLibrary() {
        Card c0 = new JamuraanLion();
        Card c1 = new Foreshadow();
        harness.setLibrary(player1, List.of(c0, c1));
        harness.castFromHand(player1, new Impulse(), "{1}{U}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(c0.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(c0);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(c1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With one card, the card goes to hand without a choice or reorder")
    void worksWithSingleCardLibrary() {
        Card onlyCard = new Warthog();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.castFromHand(player1, new Impulse(), "{1}{U}");

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(onlyCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Impulse");
    }

    @Test
    @DisplayName("With an empty library, Impulse creates no choice and goes to the graveyard")
    void worksWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new Impulse(), "{1}{U}");

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Impulse");
    }

    @Test
    @DisplayName("A mandatory Impulse choice cannot be declined")
    void cannotChooseNoCard() {
        stackFourOnTop();
        harness.castFromHand(player1, new Impulse(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid number of cards selected");
    }
}
