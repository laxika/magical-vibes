package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EnduringRenewal;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TemporaryTruce.class, GrizzlyBears.class})
class TemporaryTruceTest extends BaseCardTest {

    private void castTemporaryTruce() {
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new TemporaryTruce(), "{1}{W}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Each player draws two: no one gains life")
    void bothDrawTwo() {
        castTemporaryTruce();

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
        castTemporaryTruce();

        // player1 draws 0 -> gains 4 life; player2 draws 1 -> gains 2 life.
        harness.handleXValueChosen(player1, 0);
        harness.handleXValueChosen(player2, 1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertLife(player1, 24);
        harness.assertLife(player2, 22);
    }

    @Test
    @DisplayName("Cannot choose to draw more than two")
    void cannotDrawMoreThanTwo() {
        castTemporaryTruce();

        assertThatThrownBy(() -> harness.handleXValueChosen(player1, 3))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Both players choose their draw counts before either player draws")
    void drawChoicesPrecedeDraws() {
        castTemporaryTruce();

        harness.handleXValueChosen(player1, 2);
        int cardsInHandBeforeOpponentChooses = gd.playerHands.get(player1.getId()).size();
        harness.handleXValueChosen(player2, 0);

        assertThat(cardsInHandBeforeOpponentChooses).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 24);
    }

    @Test
    @DisplayName("Life gain waits until both players have completed the draw instruction")
    void lifeGainFollowsAllDraws() {
        castTemporaryTruce();

        harness.handleXValueChosen(player1, 0);
        int lifeBeforeOpponentChooses = gd.getLife(player1.getId());
        harness.handleXValueChosen(player2, 2);

        assertThat(lifeBeforeOpponentChooses).isEqualTo(20);
        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Both players may decline every draw and each gain four life")
    void bothDeclineDrawing() {
        castTemporaryTruce();

        harness.handleXValueChosen(player1, 0);
        harness.handleXValueChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertLife(player1, 24);
        harness.assertLife(player2, 24);
    }

    @Test
    @DisplayName("Cards drawn earlier in the turn do not reduce the life gained")
    void earlierDrawsDoNotCount() {
        castTemporaryTruce();
        gd.cardsDrawnThisTurn.put(player1.getId(), 3);
        gd.cardsDrawnThisTurn.put(player2.getId(), 4);

        harness.handleXValueChosen(player1, 1);
        harness.handleXValueChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 24);
    }

    @Test
    @CardUsed({EnduringRenewal.class})
    @DisplayName("Replaced draws count as zero cards drawn for life gain")
    void replacedDrawsGainLife() {
        harness.addToBattlefield(player1, new EnduringRenewal());
        castTemporaryTruce();

        harness.handleXValueChosen(player1, 2);
        harness.handleXValueChosen(player2, 2);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof GrizzlyBears).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
    }
}
