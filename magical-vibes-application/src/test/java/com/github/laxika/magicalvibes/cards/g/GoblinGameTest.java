package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GoblinGameTest extends BaseCardTest {

    @Test
    void eachPlayerLosesTheirCountThenFewestLosesHalfRoundedUp() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new GoblinGame()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.RED, 7);

        castGoblinGame();

        harness.handleXValueChosen(player1, 1);
        harness.handleXValueChosen(player2, 2);

        harness.assertLife(player1, 9);
        harness.assertLife(player2, 18);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void tiedFewestPlayersEachLoseHalfTheirLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new GoblinGame()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.RED, 7);

        castGoblinGame();

        harness.handleXValueChosen(player1, 2);
        harness.handleXValueChosen(player2, 2);

        harness.assertLife(player1, 9);
        harness.assertLife(player2, 9);
    }

    @Test
    void negativeLifeTotalsAreNotChangedByTheFewestPlayersHalf() {
        harness.setLife(player1, 1);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new GoblinGame()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.RED, 7);

        castGoblinGame();

        harness.handleXValueChosen(player1, 4);
        harness.handleXValueChosen(player2, 5);

        harness.assertLife(player1, -3);
        harness.assertLife(player2, 15);
    }

    private void castGoblinGame() {
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);
    }
}
