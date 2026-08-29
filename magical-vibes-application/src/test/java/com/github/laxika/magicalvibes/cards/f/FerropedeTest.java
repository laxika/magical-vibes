package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FerropedeTest extends BaseCardTest {

    @Test
    @DisplayName("Ferropede cannot be blocked")
    void cannotBeBlocked() {
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        Permanent ferropede = new Permanent(new Ferropede());
        ferropede.setSummoningSick(false);
        ferropede.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(ferropede);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Combat damage trigger may remove a counter from target permanent")
    void mayRemoveCounterFromTargetPermanent() {
        Permanent ferropede = addFerropedeReady();
        ferropede.setAttacking(true);

        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        target.setCounterCount(CounterType.CHARGE, 2);

        resolveCombat();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the combat damage trigger leaves the target counter unchanged")
    void decliningLeavesCounterUnchanged() {
        Permanent ferropede = addFerropedeReady();
        ferropede.setAttacking(true);

        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        resolveCombat();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Resolving against a target with no counters is a harmless no-op")
    void noCountersIsNoOp() {
        Permanent ferropede = addFerropedeReady();
        ferropede.setAttacking(true);

        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        resolveCombat();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).isEmpty();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addFerropedeReady() {
        Permanent ferropede = new Permanent(new Ferropede());
        ferropede.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(ferropede);
        return ferropede;
    }
}
