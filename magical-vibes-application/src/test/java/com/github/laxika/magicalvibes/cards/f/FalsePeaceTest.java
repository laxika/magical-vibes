package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RelentlessAssault;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FalsePeace.class, GrizzlyBears.class, RelentlessAssault.class})
class FalsePeaceTest extends BaseCardTest {

    private void advanceToNextTurn(com.github.laxika.magicalvibes.model.Player currentPlayer) {
        harness.forceActivePlayer(currentPlayer);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UNTAP);
    }

    @Test
    @DisplayName("Casting False Peace puts it on the stack targeting the chosen player")
    void castingTargetsPlayer() {
        harness.setHand(player1, List.of(new FalsePeace()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Resolving schedules all combat phases of the target's next turn to be skipped")
    void resolvingFlagsTargetPlayer() {
        harness.setHand(player1, List.of(new FalsePeace()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.skipCombatPhasesNextTurn).containsExactly(player2.getId());
        assertThat(gd.skipNextTurnCount).isEmpty();
        assertThat(gd.skipNextUntapStepCount).isEmpty();
        assertThat(gd.skipNextDrawStepCount).isEmpty();
    }

    @Test
    @DisplayName("The flagged player jumps from precombat main straight to postcombat main")
    void flaggedPlayerSkipsCombat() {
        // Give player2 a ready attacker so combat would otherwise halt progression.
        addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new FalsePeace()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        advanceToNextTurn(player1);
        harness.passUntil(player2, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.skipNextCombatPhaseCount.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Skips an extra combat phase created after the initial combat was skipped")
    void skipsExtraCombatPhaseCreatedAfterInitialCombatWasSkipped() {
        harness.setHand(player1, List.of(new FalsePeace()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        advanceToNextTurn(player1);
        harness.passUntil(player2, TurnStep.POSTCOMBAT_MAIN);

        harness.castFromHand(player2, new RelentlessAssault(), "{2}{R}{R}");
        harness.passBothPriorities();

        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetSelf() {
        harness.setHand(player1, List.of(new FalsePeace()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.skipCombatPhasesNextTurn).containsExactly(player1.getId());
    }
}
