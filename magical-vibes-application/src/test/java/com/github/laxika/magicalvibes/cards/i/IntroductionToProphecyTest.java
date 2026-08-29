package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IntroductionToProphecyTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new IntroductionToProphecy()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Resolving Introduction to Prophecy enters scry state with 2 cards")
    void resolvingEntersScryState() {
        castIntroductionToProphecy();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    @DisplayName("After scry completes, draws one card")
    void afterScryDrawsOneCard() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castIntroductionToProphecy();
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Scry reorders top of library before draw")
    void scryReordersBeforeDraw() {
        castIntroductionToProphecy();

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top1 = deck.get(1);

        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(top1);
    }

    @Test
    @DisplayName("Scry putting cards on bottom then drawing")
    void scryBottomThenDraw() {
        castIntroductionToProphecy();

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top2 = deck.get(2);

        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(top2);
    }

    @Test
    @DisplayName("Introduction to Prophecy goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        castIntroductionToProphecy();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Introduction to Prophecy");
    }

    private void castIntroductionToProphecy() {
        harness.setHand(player1, List.of(new IntroductionToProphecy()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, 0);
    }
}
