package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StrongarmTactics.class, Forest.class, GrizzlyBears.class})
class StrongarmTacticsTest extends BaseCardTest {

    @Test
    @DisplayName("Players who discard creature cards do not lose life")
    void creatureDiscardsAvoidLifeLoss() {
        resolveWithHands(List.of(new GrizzlyBears()), List.of(new GrizzlyBears()));

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Players who discard noncreature cards lose 4 life")
    void noncreatureDiscardsCauseLifeLoss() {
        resolveWithHands(List.of(new Forest()), List.of(new Forest()));

        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Only the player who discards a noncreature card loses life")
    void mixedDiscardsOnlyPenalizeNoncreatureDiscard() {
        resolveWithHands(List.of(new GrizzlyBears()), List.of(new Forest()));

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("A player with no card to discard loses 4 life")
    void noCardToDiscardCausesLifeLoss() {
        harness.setHand(player1, new ArrayList<>(List.of(new StrongarmTactics())));
        harness.setHand(player2, new ArrayList<>());
        castStrongarmTactics();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    private void resolveWithHands(List<com.github.laxika.magicalvibes.model.Card> controllerHand,
                                  List<com.github.laxika.magicalvibes.model.Card> opponentHand) {
        List<com.github.laxika.magicalvibes.model.Card> controllerCards = new ArrayList<>();
        controllerCards.add(new StrongarmTactics());
        controllerCards.addAll(controllerHand);
        harness.setHand(player1, controllerCards);
        harness.setHand(player2, new ArrayList<>(opponentHand));
        castStrongarmTactics();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);
    }

    private void castStrongarmTactics() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
