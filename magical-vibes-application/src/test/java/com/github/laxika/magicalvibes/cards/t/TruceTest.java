package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Truce.class})
class TruceTest extends BaseCardTest {

    private void castTruce() {
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Truce(), new Truce(), new Truce()));
        harness.setLibrary(player2, List.of(new Truce(), new Truce(), new Truce()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new Truce(), "{2}{W}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Each player draws two: no one gains life")
    void bothDrawTwo() {
        castTruce();

        // Active player (player1) chooses first.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player1, 2);
        // Then the non-active player chooses.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player2, 2);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Drawing fewer than two grants 2 life per card skipped, per player")
    void partialDrawsGainLife() {
        castTruce();

        // player1 draws 0 -> gains 4 life; player2 draws 1 -> gains 2 life.
        harness.handleXValueChosen(player1, 0);
        harness.handleXValueChosen(player2, 1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertLife(player1, 24);
        harness.assertLife(player2, 22);
    }

    @Test
    @DisplayName("Active player chooses first even when the nonactive player casts Truce")
    void activePlayerChoosesFirstWhenNonactivePlayerCasts() {
        harness.forceActivePlayer(player2);
        castTruce();

        PendingInteraction.XValueChoice choice = gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());

        harness.handleXValueChosen(player2, 0);
        harness.handleXValueChosen(player1, 2);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 24);
    }

    @Test
    @DisplayName("Cannot choose to draw more than two")
    void cannotDrawMoreThanTwo() {
        castTruce();

        assertThatThrownBy(() -> harness.handleXValueChosen(player1, 3))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
