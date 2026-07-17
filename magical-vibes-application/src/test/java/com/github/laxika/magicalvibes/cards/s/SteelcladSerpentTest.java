package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GolemsHeart;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SteelcladSerpentTest extends BaseCardTest {

    @Test
    @DisplayName("Can attack when controller controls another artifact")
    void canAttackWithAnotherArtifact() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new SteelcladSerpent());
        harness.addToBattlefield(player1, new GolemsHeart());

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }

    @Test
    @DisplayName("Cannot attack when the only artifact is itself")
    void cannotAttackWhenOnlyArtifactIsItself() {
        addCreatureReady(player1, new SteelcladSerpent());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot attack when only opponent controls another artifact")
    void cannotAttackWhenOnlyOpponentControlsArtifact() {
        addCreatureReady(player1, new SteelcladSerpent());
        harness.addToBattlefield(player2, new GolemsHeart());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices);
    }
}
