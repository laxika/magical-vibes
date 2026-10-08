package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.t.TorchCourier;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WojekBodyguard.class, TorchCourier.class})
class WojekBodyguardTest extends BaseCardTest {

    @Test
    @DisplayName("Mentor targets only an attacking creature with lesser power")
    void mentorTargetsAttackingCreatureWithLesserPower() {
        addCreatureReady(player1, new WojekBodyguard());
        Permanent attackingCourier = addCreatureReady(player1, new TorchCourier());
        Permanent nonAttackingCourier = addCreatureReady(player1, new TorchCourier());
        Permanent equalPowerCreature = addCreatureReady(player1, new WojekBodyguard());

        declareAttackers(List.of(0, 1));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(attackingCourier.getId());

        harness.handlePermanentChosen(player1, attackingCourier.getId());
        resolveAllTriggers();

        assertThat(attackingCourier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonAttackingCourier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(equalPowerCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Wojek Bodyguard cannot attack alone")
    void cannotAttackAlone() {
        addCreatureReady(player1, new WojekBodyguard());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Wojek Bodyguard cannot block alone")
    void cannotBlockAlone() {
        Permanent attacker = addCreatureReady(player1, new TorchCourier());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new WojekBodyguard());

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mentor has no legal target when only equal-power creatures attack")
    void equalPowerAttackersReceiveNoCounters() {
        Permanent first = addCreatureReady(player1, new WojekBodyguard());
        Permanent second = addCreatureReady(player1, new WojekBodyguard());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Mentor does not add a counter if the target now has equal power")
    void mentorRechecksTargetPowerOnResolution() {
        addCreatureReady(player1, new WojekBodyguard());
        Permanent courier = addCreatureReady(player1, new TorchCourier());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            harness.handlePermanentChosen(player1, courier.getId());
            assertThat(gd.stack).hasSize(1);
            courier.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
            resolveAllTriggers();
        });

        assertThat(courier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Mentor rechecks the source's current power on resolution")
    void mentorRechecksSourcePowerOnResolution() {
        Permanent bodyguard = addCreatureReady(player1, new WojekBodyguard());
        Permanent courier = addCreatureReady(player1, new TorchCourier());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            harness.handlePermanentChosen(player1, courier.getId());
            assertThat(gd.stack).hasSize(1);
            bodyguard.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
            harness.runStateBasedActions();
            resolveAllTriggers();
        });

        assertThat(courier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Mentor uses the source's power immediately before it leaves the battlefield")
    void mentorUsesLastKnownPowerAfterSourceDies() {
        Permanent bodyguard = addCreatureReady(player1, new WojekBodyguard());
        Permanent courier = addCreatureReady(player1, new TorchCourier());
        courier.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            harness.handlePermanentChosen(player1, courier.getId());
            assertThat(gd.stack).hasSize(1);
            bodyguard.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
            courier.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
            bodyguard.setMarkedDamage(4);
            harness.runStateBasedActions();
            harness.assertInGraveyard(player1, "Wojek Bodyguard");
            resolveAllTriggers();
        });

        assertThat(courier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Wojek Bodyguard can block alongside another creature")
    void canBlockWithAnotherCreature() {
        Permanent attacker = addCreatureReady(player1, new TorchCourier());
        attacker.setAttacking(true);
        Permanent bodyguard = addCreatureReady(player2, new WojekBodyguard());
        Permanent courier = addCreatureReady(player2, new TorchCourier());
        prepareDeclareBlockers();

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () ->
                gs.declareBlockers(gd, player2,
                        List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))));

        assertThat(bodyguard.isBlocking()).isTrue();
        assertThat(courier.isBlocking()).isTrue();
    }
}
