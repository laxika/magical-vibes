package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StormFleetSprinterTest extends BaseCardTest {

    @Test
    @DisplayName("Storm Fleet Sprinter can't be blocked")
    void cannotBeBlocked() {
        addCreatureReady(player2, new GrizzlyBears());
        Permanent sprinter = addCreatureReady(player1, new StormFleetSprinter());
        sprinter.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Haste lets Storm Fleet Sprinter attack the turn it enters")
    void hasteAllowsAttackWhileSummoningSick() {
        Permanent sprinter = new Permanent(new StormFleetSprinter());
        gd.playerBattlefields.get(player1.getId()).add(sprinter);
        assertThat(sprinter.isSummoningSick()).isTrue();

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }
}
