package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BronzeSable;
import com.github.laxika.magicalvibes.cards.v.Vaporkin;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HundredHandedOne.class, BronzeSable.class, Vaporkin.class})
class HundredHandedOneTest extends BaseCardTest {

    @Test
    @DisplayName("Monstrosity puts three +1/+1 counters on Hundred-Handed One")
    void monstrosityAddsCountersAndMarksItMonstrous() {
        Permanent hundredHandedOne = addReadyHundredHandedOne(player1);
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(hundredHandedOne.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(hundredHandedOne.isMonstrous()).isTrue();
        assertThat(hundredHandedOne.getEffectivePower()).isEqualTo(6);
        assertThat(hundredHandedOne.getEffectiveToughness()).isEqualTo(8);
    }

    @Test
    @DisplayName("Monstrous Hundred-Handed One has reach and can block two attackers")
    void monstrousAbilitiesApply() {
        Permanent hundredHandedOne = addReadyHundredHandedOne(player2);
        activateMonstrosity(hundredHandedOne);

        assertThat(gqs.hasKeyword(gd, hundredHandedOne, Keyword.REACH)).isTrue();

        addAttacker(player1);
        addAttacker(player1);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(hundredHandedOne);

        prepareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIndex, 0),
                new BlockerAssignment(blockerIndex, 1)
        ));

        assertThat(hundredHandedOne.getBlockingTargets()).containsExactlyInAnyOrder(0, 1);
    }

    @Test
    @DisplayName("Monstrosity can be activated again but does nothing once monstrous")
    void monstrosityCanBeActivatedAgain() {
        Permanent hundredHandedOne = addReadyHundredHandedOne(player1);
        activateMonstrosity(hundredHandedOne);
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(hundredHandedOne.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(hundredHandedOne.isMonstrous()).isTrue();
    }

    @Test
    void twoPendingMonstrosityAbilitiesOnlyAddCountersOnce() {
        Permanent creature = addReadyHundredHandedOne(player1);
        harness.addMana(player1, ManaColor.WHITE, 12);
        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(creature.isMonstrous()).isTrue();
    }

    @Test
    void nonMonstrousCreatureCannotBlockTwoAttackers() {
        Permanent creature = addReadyHundredHandedOne(player2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isFalse();
        addAttacker(player1);
        addAttacker(player1);
        prepareBlockers();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void monstrousCreatureCanBlockOneHundredAttackers() {
        Permanent creature = addReadyHundredHandedOne(player2);
        activateMonstrosity(creature);
        for (int i = 0; i < 100; i++) {
            addAttacker(player1);
        }
        prepareBlockers();
        gs.declareBlockers(gd, player2, IntStream.range(0, 100)
                .mapToObj(i -> new BlockerAssignment(0, i)).toList());
        assertThat(creature.getBlockingTargets()).hasSize(100);
    }

    @Test
    void monstrousCreatureCannotBlockOneHundredAndOneAttackers() {
        Permanent creature = addReadyHundredHandedOne(player2);
        activateMonstrosity(creature);
        for (int i = 0; i < 101; i++) {
            addAttacker(player1);
        }
        prepareBlockers();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, IntStream.range(0, 101)
                .mapToObj(i -> new BlockerAssignment(0, i)).toList()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }

    @Test
    void monstrousCreatureCanBlockFlyingAttacker() {
        Permanent creature = addReadyHundredHandedOne(player2);
        activateMonstrosity(creature);
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new Vaporkin());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        prepareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(creature.getBlockingTargets()).containsExactly(0);
    }

    @Test
    void nonMonstrousCreatureCannotBlockFlyingAttacker() {
        addReadyHundredHandedOne(player2);
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new Vaporkin());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        prepareBlockers();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyHundredHandedOne(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new HundredHandedOne());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void activateMonstrosity(Permanent hundredHandedOne) {
        Player controller = gd.playerBattlefields.get(player1.getId()).contains(hundredHandedOne) ? player1 : player2;
        int index = gd.playerBattlefields.get(controller.getId()).indexOf(hundredHandedOne);
        harness.forceActivePlayer(controller);
        harness.addMana(controller, ManaColor.WHITE, 6);
        harness.activateAbility(controller, index, null, null);
        harness.passBothPriorities();
    }

    private void addAttacker(Player player) {
        Permanent attacker = harness.addToBattlefieldAndReturn(player, new BronzeSable());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
    }
}
