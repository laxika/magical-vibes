package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MomentOfSilence.class, FreshVolunteers.class})
class MomentOfSilenceTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Moment of Silence targets the chosen player")
    void castingTargetsPlayer() {
        harness.setHand(player1, List.of(new MomentOfSilence()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Resolving Moment of Silence flags the target player to skip combat")
    void resolvingFlagsTargetPlayer() {
        harness.setHand(player1, List.of(new MomentOfSilence()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.skipNextCombatPhaseCount.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.skipNextCombatPhaseCount.getOrDefault(player1.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("The flagged player jumps from precombat main straight to postcombat main")
    void flaggedPlayerSkipsCombat() {
        addCreatureReady(player2, new FreshVolunteers());

        gd.skipNextCombatPhaseCount.put(player2.getId(), 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.skipNextCombatPhaseCount.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Moment of Silence can target its caster")
    void canTargetSelf() {
        harness.setHand(player1, List.of(new MomentOfSilence()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.skipNextCombatPhaseCount.getOrDefault(player1.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("A skip cast after combat does not carry into the next turn")
    void skipExpiresAtEndOfTurn() {
        addCreatureReady(player2, new FreshVolunteers());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new MomentOfSilence()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.skipNextCombatPhaseCount.getOrDefault(player2.getId(), 0)).isEqualTo(1);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.skipNextCombatPhaseCount.getOrDefault(player2.getId(), 0)).isZero();
        harness.passBothPriorities();
        assertThat(gd.currentStep.getPhaseName()).isEqualTo("Combat Phase");
    }
}
