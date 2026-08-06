package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArrowsOfJusticeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to an attacking creature, killing it")
    void killsAttacker() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        castArrows(attacker);

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A blocking creature is a legal target")
    void killsBlocker() {
        harness.forceActivePlayer(player1);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        castArrows(blocker);

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature that is neither attacking nor blocking")
    void cannotTargetIdleCreature() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent idle = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ArrowsOfJustice()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, idle.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an attacking or blocking creature");
    }

    private void castArrows(final Permanent target) {
        harness.setHand(player1, List.of(new ArrowsOfJustice()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
