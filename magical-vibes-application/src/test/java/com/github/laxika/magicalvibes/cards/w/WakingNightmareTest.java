package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HumbleBudoka;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WakingNightmare.class, WanderingOnes.class, HumbleBudoka.class, Forest.class})
class WakingNightmareTest extends BaseCardTest {

    @Test
    @DisplayName("Target player discards two cards of their choice")
    void targetDiscardsTwoCards() {
        harness.setHand(player2, List.of(new WanderingOnes(), new HumbleBudoka(), new Forest()));
        harness.setHand(player1, List.of(new WakingNightmare()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Wandering Ones");
        harness.assertInGraveyard(player2, "Humble Budoka");
        harness.assertInGraveyard(player1, "Waking Nightmare");
    }

    @Test
    @DisplayName("Target with a single card discards only that card")
    void targetWithOneCard() {
        harness.setHand(player2, List.of(new WanderingOnes()));
        harness.setHand(player1, List.of(new WakingNightmare()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Wandering Ones");
    }

    @Test
    @DisplayName("Target with an empty hand is never prompted")
    void targetWithEmptyHand() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new WakingNightmare()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The caster cannot make the discard choice for the target")
    void casterCannotChooseForTarget() {
        harness.setHand(player2, List.of(new WanderingOnes(), new HumbleBudoka()));
        harness.setHand(player1, List.of(new WakingNightmare()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn to choose");
    }

    @Test
    @DisplayName("The controller can be the target")
    void controllerCanBeTarget() {
        harness.setHand(player1, List.of(new WakingNightmare(), new WanderingOnes(), new HumbleBudoka()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Waking Nightmare");
        harness.assertInGraveyard(player1, "Wandering Ones");
        harness.assertInGraveyard(player1, "Humble Budoka");
    }
}
