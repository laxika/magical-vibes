package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AxegrinderGiant;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BlindSpotGiantTest extends BaseCardTest {

    // ===== Attack restriction =====

    @Test
    @DisplayName("Can attack when controlling another Giant")
    void canAttackWithAnotherGiant() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new BlindSpotGiant());
        addCreatureReady(player1, new AxegrinderGiant());

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }

    @Test
    @DisplayName("Cannot attack when it is the only Giant controlled")
    void cannotAttackAsOnlyGiant() {
        addCreatureReady(player1, new BlindSpotGiant());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot attack when only the opponent controls another Giant")
    void cannotAttackWhenOnlyOpponentControlsGiant() {
        addCreatureReady(player1, new BlindSpotGiant());
        addCreatureReady(player2, new AxegrinderGiant());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Block restriction =====

    @Test
    @DisplayName("Can block when controlling another Giant")
    void canBlockWithAnotherGiant() {
        addCreatureReady(player2, new AxegrinderGiant());
        addCreatureReady(player1, new BlindSpotGiant());
        addCreatureReady(player1, new AxegrinderGiant());

        declareAttackers(player2, List.of(0));

        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Cannot block when it is the only Giant controlled")
    void cannotBlockAsOnlyGiant() {
        addCreatureReady(player2, new AxegrinderGiant());
        addCreatureReady(player1, new BlindSpotGiant());

        declareAttackers(player2, List.of(0));

        harness.beginBlockerDeclarationInput();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helper methods =====

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices);
    }
}
