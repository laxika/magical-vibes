package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({MindSpike.class, Forest.class, GrizzlyBears.class, Shock.class})
class MindSpikeTest extends BaseCardTest {

    @Test
    @DisplayName("The only revealed noncreature, nonland card is discarded unasked, then the caster loses 2 life")
    void discardsTheOnlyRevealedCardThenLosesLife() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        cast(List.of(forest, bears, shock));

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(forest, bears);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(shock);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Draws a card when the target reveals no noncreature, nonland card")
    void drawsWhenTargetHasNoNoncreatureNonlandCards() {
        Card drawn = new Shock();
        harness.setLibrary(player1, List.of(drawn));
        cast(List.of(new Forest(), new GrizzlyBears()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Can target only an opponent")
    void canOnlyTargetAnOpponent() {
        harness.setHand(player1, List.of(new MindSpike()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Draws a card when the target's hand is empty")
    void drawsWhenTargetHandIsEmpty() {
        Card drawn = new Shock();
        harness.setLibrary(player1, List.of(drawn));

        cast(List.of());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The caster chooses exactly one of the revealed cards, and doesn't draw")
    void casterMustChooseExactlyOneEligibleCardWithoutDrawing() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        Card firstShock = new Shock();
        Card secondShock = new Shock();
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        cast(List.of(forest, firstShock, bears, secondShock));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(firstShock.getId(), secondShock.getId());
        harness.assertLife(player1, 20);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player2, List.of(firstShock.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(secondShock.getId()));

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(forest, firstShock, bears);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(secondShock);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void cast(List<Card> targetHand) {
        harness.setHand(player1, List.of(new MindSpike()));
        harness.setHand(player2, targetHand);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }
}
