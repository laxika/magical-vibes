package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MedomaiTheAgeless.class})
class MedomaiTheAgelessTest extends BaseCardTest {

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Combat damage queues an extra turn, where Medomai cannot attack")
    void combatDamageQueuesExtraTurnAndPreventsAttackDuringIt() {
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            Permanent medomai = addCreatureReady(player1, new MedomaiTheAgeless());

            declareAttackers(player1, List.of(0));
            resolveAllTriggers();

            assertThat(gd.extraTurns).containsExactly(player1.getId());

            advanceTurn();

            assertThat(gd.currentTurnIsExtraTurn).isTrue();
            assertThat(medomai.isTapped()).isFalse();
            assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Invalid attacker index");
        });
    }

    @Test
    @DisplayName("Combat damage grants the extra turn to Medomai's controller")
    void opponentControlledMedomaiGrantsOpponentExtraTurn() {
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            addCreatureReady(player2, new MedomaiTheAgeless());

            declareAttackers(player2, List.of(0));
            resolveAllTriggers();

            assertThat(gd.extraTurns).containsExactly(player2.getId());
            advanceTurn();
            assertThat(gd.activePlayerId).isEqualTo(player2.getId());
            assertThat(gd.currentTurnIsExtraTurn).isTrue();
        });
    }

    @Test
    @DisplayName("Blocked combat damage does not grant either player an extra turn")
    void blockedCombatDoesNotGrantExtraTurn() {
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            addCreatureReady(player1, new MedomaiTheAgeless());
            addCreatureReady(player2, new MedomaiTheAgeless());

            declareAttackersAndPrepareBlockers(player1, List.of(0));
            gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
            resolveCombat();
            resolveAllTriggers();

            assertThat(gd.extraTurns).isEmpty();
            harness.assertNotOnBattlefield(player1, "Medomai the Ageless");
            harness.assertNotOnBattlefield(player2, "Medomai the Ageless");
        });
    }

    @Test
    @DisplayName("Medomai can attack again on its controller's next normal turn")
    void canAttackAgainAfterExtraTurnEnds() {
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            addCreatureReady(player1, new MedomaiTheAgeless());
            declareAttackers(player1, List.of(0));
            resolveAllTriggers();

            advanceTurn();
            assertThat(gd.currentTurnIsExtraTurn).isTrue();
            advanceTurn();
            assertThat(gd.activePlayerId).isEqualTo(player2.getId());
            assertThat(gd.currentTurnIsExtraTurn).isFalse();
            advanceTurn();
            assertThat(gd.activePlayerId).isEqualTo(player1.getId());
            assertThat(gd.currentTurnIsExtraTurn).isFalse();

            declareAttackers(player1, List.of(0));
            resolveAllTriggers();
            assertThat(gd.extraTurns).containsExactly(player1.getId());
        });
    }
}
