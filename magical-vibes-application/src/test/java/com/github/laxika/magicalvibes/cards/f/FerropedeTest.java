package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Ferropede.class, Forest.class})
class FerropedeTest extends BaseCardTest {

    @Test
    @DisplayName("Ferropede cannot be blocked")
    void cannotBeBlocked() {
        addCreatureReady(player2, new Ferropede());

        Permanent ferropede = addCreatureReady(player1, new Ferropede());
        ferropede.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Combat damage trigger may remove a counter from target permanent")
    void mayRemoveCounterFromTargetPermanent() {
        Permanent ferropede = addCreatureReady(player1, new Ferropede());
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
        Permanent ferropede = addCreatureReady(player1, new Ferropede());
        ferropede.setAttacking(true);

        Permanent target = harness.addToBattlefieldAndReturn(player2, new Ferropede());
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
        Permanent ferropede = addCreatureReady(player1, new Ferropede());
        ferropede.setAttacking(true);

        Permanent target = harness.addToBattlefieldAndReturn(player2, new Ferropede());

        resolveCombat();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).isEmpty();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Ferropede's controller chooses which counter type to remove")
    void controllerChoosesCounterType() {
        Permanent ferropede = addCreatureReady(player1, new Ferropede());
        ferropede.setAttacking(true);

        Permanent target = harness.addToBattlefieldAndReturn(player2, new Ferropede());
        target.setCounterCount(CounterType.CHARGE, 2);
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        resolveCombat();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "+1/+1 counters");

        assertThat(target.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
