package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.l.LeatherbackBaloth;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FeralContest.class, LeatherbackBaloth.class})
class FeralContestTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a counter on the first target and requires the second target to block it")
    void putsCounterAndRequiresBlock() {
        Permanent attacker = addCreatureReady(player1, new LeatherbackBaloth());
        Permanent blocker = addCreatureReady(player2, new LeatherbackBaloth());

        castFeralContest(attacker, blocker);

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Requires distinct targets")
    void requiresDistinctTargets() {
        Permanent target = addCreatureReady(player1, new LeatherbackBaloth());
        harness.setHand(player1, List.of(new FeralContest()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(target.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different");
    }

    @Test
    @DisplayName("The counter target must be a creature you control")
    void counterTargetMustBeControlled() {
        Permanent opponentCreature = addCreatureReady(player2, new LeatherbackBaloth());
        Permanent blocker = addCreatureReady(player1, new LeatherbackBaloth());
        harness.setHand(player1, List.of(new FeralContest()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(opponentCreature.getId(), blocker.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    @Test
    void tappedCreatureIsNotRequiredToBlock() {
        Permanent attacker = addCreatureReady(player1, new LeatherbackBaloth());
        Permanent blocker = addCreatureReady(player2, new LeatherbackBaloth());
        blocker.tap();

        castFeralContest(attacker, blocker);
        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of());
        assertThat(blocker.isBlocking()).isFalse();
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void secondTargetMayAlsoBeControlledByCaster() {
        Permanent counterTarget = addCreatureReady(player1, new LeatherbackBaloth());
        Permanent otherCreature = addCreatureReady(player1, new LeatherbackBaloth());

        castFeralContest(counterTarget, otherCreature);

        assertThat(counterTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void counterIsAddedWhenSecondTargetLeavesBeforeResolution() {
        Permanent counterTarget = addCreatureReady(player1, new LeatherbackBaloth());
        Permanent blocker = addCreatureReady(player2, new LeatherbackBaloth());
        harness.setHand(player1, List.of(new FeralContest()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castSorcery(player1, 0, List.of(counterTarget.getId(), blocker.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(blocker);
        harness.passBothPriorities();

        assertThat(counterTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void noBlockingRequirementWhenFirstTargetLeavesBeforeResolution() {
        Permanent counterTarget = addCreatureReady(player1, new LeatherbackBaloth());
        Permanent otherAttacker = addCreatureReady(player1, new LeatherbackBaloth());
        Permanent blocker = addCreatureReady(player2, new LeatherbackBaloth());
        harness.setHand(player1, List.of(new FeralContest()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castSorcery(player1, 0, List.of(counterTarget.getId(), blocker.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(counterTarget);
        harness.passBothPriorities();
        otherAttacker.setAttacking(true);
        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of());

        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    void blockingAnotherAttackerDoesNotSatisfyRequirement() {
        Permanent counterTarget = addCreatureReady(player1, new LeatherbackBaloth());
        Permanent otherAttacker = addCreatureReady(player1, new LeatherbackBaloth());
        Permanent blocker = addCreatureReady(player2, new LeatherbackBaloth());
        castFeralContest(counterTarget, blocker);
        counterTarget.setAttacking(true);
        otherAttacker.setAttacking(true);
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
    }

    @Test
    void blockerMayBlockAnotherAttackerWhenCounterTargetDoesNotAttack() {
        Permanent counterTarget = addCreatureReady(player1, new LeatherbackBaloth());
        Permanent otherAttacker = addCreatureReady(player1, new LeatherbackBaloth());
        Permanent blocker = addCreatureReady(player2, new LeatherbackBaloth());
        castFeralContest(counterTarget, blocker);
        otherAttacker.setAttacking(true);
        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private void castFeralContest(Permanent counterTarget, Permanent blockerTarget) {
        harness.setHand(player1, List.of(new FeralContest()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveSorcery(player1, 0, List.of(counterTarget.getId(), blockerTarget.getId()));
    }
}
