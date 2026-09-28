package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.ForestBear;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WolverineClawsOut.class, GrizzlyBears.class, ForestBear.class})
class WolverineClawsOutTest extends BaseCardTest {

    @Test
    @DisplayName("A Mutant you control attacking doubles its power until end of turn")
    void mutantAttackerDoublesPower() {
        Permanent wolverine = addCreatureReady(player1, new WolverineClawsOut());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wolverine)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, wolverine)).isEqualTo(5);
    }

    @Test
    @DisplayName("A non-Mutant attacker does not trigger the power doubling")
    void nonMutantAttackerDoesNotTrigger() {
        addCreatureReady(player1, new WolverineClawsOut());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Wolverine may assign combat damage as though it were unblocked")
    void blockedWolverineAssignsDamageToDefendingPlayer() {
        harness.setLife(player2, 20);
        Permanent wolverine = addCreatureReady(player1, new WolverineClawsOut());
        Permanent blocker = addCreatureReady(player2, new ForestBear());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(wolverine))));
        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 4));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        harness.assertOnBattlefield(player2, "Forest Bear");
    }

    @Test
    @DisplayName("The power doubling wears off at end of turn")
    void powerDoublingWearsOffAtEndOfTurn() {
        Permanent wolverine = addCreatureReady(player1, new WolverineClawsOut());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, wolverine)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wolverine)).isEqualTo(2);
    }
}
