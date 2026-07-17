package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameOfChaosTest extends BaseCardTest {

    @Test
    @DisplayName("First flip swaps 1 life between controller and opponent; winner may flip again")
    void firstFlipSwapsOneLife() {
        int c0 = gd.playerLifeTotals.get(player1.getId());
        int o0 = gd.playerLifeTotals.get(player2.getId());

        harness.setHand(player1, List.of(new GameOfChaos()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve first flip -> flip-again prompt

        int c1 = gd.playerLifeTotals.get(player1.getId());
        int o1 = gd.playerLifeTotals.get(player2.getId());

        // Exactly one participant gained 1 life and the other lost 1.
        assertThat(c1 - c0).isIn(1, -1);
        assertThat(o1 - o0).isEqualTo(-(c1 - c0));

        // The flip's winner (whoever gained life) decides whether to flip again.
        UUID decider = gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId();
        UUID expectedDecider = (c1 > c0) ? player1.getId() : player2.getId();
        assertThat(decider).isEqualTo(expectedDecider);
    }

    @Test
    @DisplayName("Declining the flip-again prompt ends the loop with life totals unchanged")
    void decliningEndsLoop() {
        harness.setHand(player1, List.of(new GameOfChaos()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        int c1 = gd.playerLifeTotals.get(player1.getId());
        int o1 = gd.playerLifeTotals.get(player2.getId());

        Player decider = deciderPlayer();
        harness.handleMayAbilityChosen(decider, false);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(c1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(o1);
    }

    @Test
    @DisplayName("Accepting flips again with the life stakes doubled to 2")
    void acceptingDoublesStakes() {
        harness.setHand(player1, List.of(new GameOfChaos()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        int c1 = gd.playerLifeTotals.get(player1.getId());
        int o1 = gd.playerLifeTotals.get(player2.getId());

        harness.handleMayAbilityChosen(deciderPlayer(), true); // flip again at stakes 2

        int c2 = gd.playerLifeTotals.get(player1.getId());
        int o2 = gd.playerLifeTotals.get(player2.getId());

        // The second flip moves each participant's life by exactly 2.
        assertThat(Math.abs(c2 - c1)).isEqualTo(2);
        assertThat(o2 - o1).isEqualTo(-(c2 - c1));

        // The second flip's winner is prompted to flip again.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Cannot target self — must target an opponent")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new GameOfChaos()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Player deciderPlayer() {
        UUID decider = gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId();
        return decider.equals(player1.getId()) ? player1 : player2;
    }
}
