package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BriarpackAlpha;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CheckForTraps.class, BriarpackAlpha.class, Forest.class, GrizzlyBears.class, Peek.class})
class CheckForTrapsTest extends BaseCardTest {

    @Test
    @DisplayName("Exiling an instant makes the opponent lose 1 life")
    void exiledInstantMakesOpponentLoseLife() {
        castWithHand(new Peek(), new Forest());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .singleElement().isInstanceOf(Peek.class);
    }

    @Test
    @DisplayName("Exiling a card with flash makes the opponent lose 1 life")
    void exiledFlashCardMakesOpponentLoseLife() {
        castWithHand(new BriarpackAlpha(), new Forest());

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Exiling another nonland card makes the controller lose 1 life")
    void exiledOtherCardMakesControllerLoseLife() {
        castWithHand(new GrizzlyBears(), new Forest());

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player2.getId()))
                .singleElement().isInstanceOf(Forest.class);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new CheckForTraps()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("An empty hand still makes the controller lose 1 life")
    void emptyHandMakesControllerLoseLife() {
        castWithHand();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A hand containing only lands still makes the controller lose 1 life")
    void landsOnlyMakesControllerLoseLife() {
        Forest first = new Forest();
        Forest second = new Forest();
        castWithHand(first, second);

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(first, second);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The caster cannot choose a land from the revealed hand")
    void cannotChooseLand() {
        castWithHand(new Peek(), new Forest());

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 1))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();

        harness.handleCardChosen(player1, 0);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("The caster chooses exactly one nonland card, rather than the opponent")
    void casterChoosesOneNonlandCard() {
        Peek instant = new Peek();
        GrizzlyBears creature = new GrizzlyBears();
        Forest land = new Forest();
        castWithHand(instant, creature, land);

        assertThatThrownBy(() -> harness.handleCardChosen(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.handleCardChosen(player1, 1);

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(instant, land);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castWithHand(Card... cards) {
        harness.setHand(player1, List.of(new CheckForTraps()));
        harness.setHand(player2, List.of(cards));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }
}
