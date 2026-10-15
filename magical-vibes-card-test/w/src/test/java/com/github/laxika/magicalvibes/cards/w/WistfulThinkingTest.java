package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.s.SerraSphinx;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WistfulThinking.class, SerraSphinx.class})
class WistfulThinkingTest extends BaseCardTest {

    @Test
    @DisplayName("Target player draws two cards, then discards four cards")
    void drawsTwoThenDiscardsFour() {
        harness.setHand(player1, List.of(new WistfulThinking()));
        harness.setHand(player2, List.of(new SerraSphinx(), new SerraSphinx(), new SerraSphinx()));
        harness.setLibrary(player2, List.of(new SerraSphinx(), new SerraSphinx()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).hasSize(5);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("The controller may be targeted and discards their whole hand when it has fewer than four cards")
    void controllerWithFewerThanFourCardsDiscardsWholeHand() {
        harness.setHand(player1, List.of(new WistfulThinking(), new SerraSphinx()));
        harness.setLibrary(player1, List.of(new SerraSphinx(), new SerraSphinx()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("An empty-handed target draws two cards and then discards both")
    void emptyHandedTargetDiscardsTheDrawnCards() {
        var firstDraw = new SerraSphinx();
        var secondDraw = new SerraSphinx();
        harness.setHand(player1, List.of(new WistfulThinking()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(firstDraw, secondDraw));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).containsExactlyInAnyOrder(firstDraw, secondDraw);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyInAnyOrder(firstDraw, secondDraw);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Wistful Thinking");
    }

    @Test
    @DisplayName("Wistful Thinking cannot target a permanent")
    void cannotTargetPermanent() {
        harness.setHand(player1, List.of(new WistfulThinking()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        var sphinx = harness.addToBattlefieldAndReturn(player2, new SerraSphinx());

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, sphinx.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
