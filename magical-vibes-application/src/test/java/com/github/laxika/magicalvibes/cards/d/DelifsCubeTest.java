package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.i.IcatianPhalanx;
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

@CardUsed({DelifsCube.class, IcatianPhalanx.class})
class DelifsCubeTest extends BaseCardTest {

    @Test
    @DisplayName("An unblocked chosen attacker adds a cube counter and assigns no combat damage")
    void unblockedChosenAttackerAddsCubeCounterAndDealsNoCombatDamage() {
        Permanent cube = harness.addToBattlefieldAndReturn(player1, new DelifsCube());
        Permanent attacker = addCreatureReady(player1, new IcatianPhalanx());
        addCreatureReady(player2, new IcatianPhalanx());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));

        declareBlockers(List.of());

        assertThat(cube.getCounterCount(CounterType.CUBE)).isEqualTo(1);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());

        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A blocked chosen attacker does not add a cube counter or lose combat damage")
    void blockedChosenAttackerDoesNotTrigger() {
        Permanent cube = harness.addToBattlefieldAndReturn(player1, new DelifsCube());
        Permanent attacker = addCreatureReady(player1, new IcatianPhalanx());
        Permanent blocker = addCreatureReady(player2, new IcatianPhalanx());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        declareBlockers(List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(cube.getCounterCount(CounterType.CUBE)).isZero();
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());
        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Removing a cube counter regenerates a target creature")
    void removingCubeCounterRegeneratesTargetCreature() {
        Permanent cube = harness.addToBattlefieldAndReturn(player1, new DelifsCube());
        cube.setCounterCount(CounterType.CUBE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IcatianPhalanx());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(cube.getCounterCount(CounterType.CUBE)).isZero();
        assertThat(target.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("The first ability only targets a creature you control")
    void firstAbilityOnlyTargetsCreatureYouControl() {
        harness.addToBattlefield(player1, new DelifsCube());
        Permanent opponentCreature = addCreatureReady(player2, new IcatianPhalanx());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Activating after blockers are declared does not create the delayed trigger")
    void activatingAfterBlockersAreDeclaredDoesNotTrigger() {
        Permanent cube = harness.addToBattlefieldAndReturn(player1, new DelifsCube());
        Permanent attacker = addCreatureReady(player1, new IcatianPhalanx());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(cube.getCounterCount(CounterType.CUBE)).isZero();
        resolveCombat();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The delayed trigger still adds a counter when the watched attacker leaves")
    void delayedTriggerStillAddsCounterWhenWatchedAttackerLeaves() {
        Permanent cube = harness.addToBattlefieldAndReturn(player1, new DelifsCube());
        Permanent attacker = addCreatureReady(player1, new IcatianPhalanx());
        addCreatureReady(player2, new IcatianPhalanx());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(harness.getPermanentRemovalService().removePermanentToGraveyard(gd, attacker)).isTrue();
        harness.passBothPriorities();

        assertThat(cube.getCounterCount(CounterType.CUBE)).isEqualTo(1);
    }

    private void declareBlockers(List<BlockerAssignment> assignments) {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, assignments);
        harness.passBothPriorities();
    }
}
